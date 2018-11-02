package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.inviscidlabs.enkel.custom.PlayRequestEvent
import com.inviscidlabs.enkel.custom.RxEventBus
import com.inviscidlabs.enkel.custom.TimerExpiredEvent
import com.inviscidlabs.enkel.custom.TimerTickRxEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class TimerViewModel(private val timerID: Int, private val secondsToCountdown: Long): ViewModel(){

    private var disposableTick: Disposable? = null
    private var disposableFinished: Disposable? = null

    //Mutable, local variables
     private val _timeRemaining = MutableLiveData<Long>()
     private val _isPaused = MutableLiveData<Boolean>()
     private val _timeIsExpired = MutableLiveData<Boolean>()

    //Immutable public variables
    val timeRemaining: LiveData<Long> get() = _timeRemaining
    val isPaused: LiveData<Boolean> get() = _isPaused
    val timeIsExpired: LiveData<Boolean> get() = _timeIsExpired

    init {
        _timeRemaining.value = secondsToCountdown
        _isPaused.value=true
        if(_timeIsExpired.value==null){
            _timeIsExpired.value = false
        }
        _timeRemaining.postValue(secondsToCountdown)
        listenForTickFromService()
        listenForTimerFinishedFromService()
    }

    override fun onCleared() {
        disposableTick?.dispose()
        disposableFinished?.dispose()
        super.onCleared()
    }

//region 2nd layer functions
    //TODO observe only on thread specified by timerID
    private fun listenForTickFromService() {
        disposableTick = RxEventBus.subscribe<TimerTickRxEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==timerID){
                        _timeRemaining.value = it.timeRemainingInSeconds
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

    fun setPauseStatus(pause: Boolean){
        _isPaused.value = pause
        RxEventBus.post(PlayRequestEvent(timerID = timerID, isPaused = pause))
    }

    fun resetTimer(){
        setPauseStatus(true)
    }
//endregion

    class Factory(private val timerID: Int, private val totalTimeToCountdownInSeconds: Long): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = TimerViewModel(timerID, totalTimeToCountdownInSeconds) as T
    }

}