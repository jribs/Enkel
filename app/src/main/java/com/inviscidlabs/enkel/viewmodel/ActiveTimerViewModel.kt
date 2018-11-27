package com.inviscidlabs.enkel.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.content.Intent
import com.inviscidlabs.enkel.app.*
import com.inviscidlabs.enkel.viewmodel.service.EnkelTimerService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class ActiveTimerViewModel(private val timerID: Int, private val secondsToCountdown: Long, private val app: Application):
        AndroidViewModel(app){

    private var disposableTick: Disposable? = null
    private var disposableFinished: Disposable? = null
    private var disposableTimerStatus: Disposable? = null
    private var disposablePlayPause: Disposable? = null

    //Mutable, local variables
     private val _timeRemaining = MutableLiveData<Long>()
     private val _isPaused = MutableLiveData<Boolean>()
     private val _timeIsExpired = MutableLiveData<Boolean>()

    //Immutable public variables
    val timeRemaining: LiveData<Long> get() = _timeRemaining
    val isPaused: LiveData<Boolean> get() = _isPaused
    val timeIsExpired: LiveData<Boolean> get() = _timeIsExpired

    init {
        listenForTimerStatus()
        _timeRemaining.value = secondsToCountdown
        if(_timeIsExpired.value==null){
            _timeIsExpired.value = false
        }
        _timeRemaining.postValue(secondsToCountdown)
        listenForTickFromService()
        listenForTimerFinishedFromService()
        listenForPlayPauseStatus()
        emitRequestForTimerStatus()
    }

    override fun onCleared() {
        disposableTick?.dispose()
        disposableFinished?.dispose()
        super.onCleared()
    }

//region UI
    //TODO remove setting isPaused. Change only when broadcast received
    fun setPauseStatus(isPaused: Boolean){
        if (secondsToCountdown == _timeRemaining.value && !isPaused) {
            startNewTimerInService(isPaused)
        } else {
            RxEventBus.post(PlayRequestEvent(timerID = timerID))
        }
    }

    fun resetTimer(){
        RxEventBus.post(ResetTimerEvent(timerID))
    }
//endregion

//region 2nd layer functions
    //TODO observe only on thread specified by timerID
    private fun listenForTickFromService() {
        disposableTick = RxEventBus.subscribe<TimerTickRxEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==timerID){
                        _timeRemaining.postValue(it.timeRemainingInSeconds)
                    }
                }
    }

    private fun listenForTimerFinishedFromService() {
        disposableFinished = RxEventBus.subscribe<TimerExpiredEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==timerID) {
                        _timeIsExpired.postValue(true)
                    }
                }
    }

    private fun listenForTimerStatus(){
        disposableTimerStatus = RxEventBus.subscribe<ProvideTimerStatusEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID == timerID){
                        _timeRemaining.postValue(it.timeRemainingInSeconds/1000)
                        if(_isPaused.value !=it.isPaused){
                            _isPaused.postValue(it.isPaused)
                        }
                    }
                }
    }

    private fun emitRequestForTimerStatus() {
        RxEventBus.post(RequestTimerStatusEvent(timerID))
    }

    private fun listenForPlayPauseStatus(){
        disposablePlayPause = RxEventBus.subscribe<PlayPauseOutputEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    if(it.timerID==timerID){
                        _isPaused.postValue(it.isPaused)
                    }
                }
    }
//endregion

//region Utility Functions
    private fun startNewTimerInService(isPaused: Boolean){
        val playPauseIntent = Intent(app.applicationContext, EnkelTimerService::class.java).apply {
            putExtra(INTENT_TIMERID, timerID.toLong())
            putExtra(INTENT_TIMERTIME, secondsToCountdown)
            action = ACTION_START_TIMER
        }
        app.applicationContext.startService(playPauseIntent)
    }
//endregion

    class Factory(private val timerID: Int, private val totalTimeToCountdownInSeconds: Long,
                  private val app: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = ActiveTimerViewModel(timerID, totalTimeToCountdownInSeconds, app) as T
    }
}