package com.inviscidlabs.enkel.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.content.Intent
import com.inviscidlabs.enkel.app.*
import com.inviscidlabs.enkel.viewmodel.service.EnkelTimerService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class ActiveTimerViewModel(private val timerID: Int, private val secondsToCountdown: Long): ViewModel() {

    private var disposableTick: Disposable? = null
    private var disposableFinished: Disposable? = null
    private var disposableTimerStatus: Disposable? = null

    //Mutable, local variables
     private val _timeRemaining = MutableLiveData<Long>()
     private val _timeIsExpired = MutableLiveData<Boolean>()

    //Immutable public variables
    val timeRemaining: LiveData<Long> get() = _timeRemaining
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
        emitRequestForTimerStatus()
    }

    override fun onCleared() {
        disposableTick?.dispose()
        disposableFinished?.dispose()
        super.onCleared()
    }

    fun timerExpired(){
        _timeRemaining.value = secondsToCountdown
    }

//region 2nd layer functions
    //TODO observe only on thread specified by timerID
    private fun listenForTickFromService() {
        disposableTick = RxEventBus.subscribe<TimerTickRxEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==timerID){
                        _timeRemaining.postValue(it.timeRemainingInSeconds/1000)
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
                    }
                }
    }

    private fun emitRequestForTimerStatus() {
        RxEventBus.post(RequestTimerStatusEvent(timerID))
    }

//endregion

    class Factory(private val timerID: Int, private val secondsToCountdown: Long): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T
                = ActiveTimerViewModel(timerID, secondsToCountdown) as T
    }
}