package com.inviscidlabs.enkel.ViewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.inviscidlabs.enkel.EnkelTimer


class TimerViewModel(private val secondsToCountdown: Long): ViewModel(){

    //Mutable, local variables
     private val _timeElapsed = MutableLiveData<Long>()
     private val _isPaused = MutableLiveData<Boolean>()
     private val _timeIsExpired = MutableLiveData<Boolean>()

    //Immutable public variables
    val timeElapsed: LiveData<Long> get() = _timeElapsed
    val isPaused: LiveData<Boolean> get() = _isPaused
    val timeIsExpired: LiveData<Boolean> get() = _timeIsExpired


    init {
        _timeElapsed.value = secondsToCountdown
        _isPaused.value=true
        if(_timeIsExpired.value==null){
            _timeIsExpired.value = false
        }
    }

    private val timer = object: EnkelTimer(secondsToCountdown*1000, 1000) {
        override fun onFinish() {
            _timeIsExpired.value = true
        }

        override fun onTick(millisUntilFinished: Long) {
            _timeElapsed.value = millisUntilFinished/1000
        }
    }

    fun setPauseStatus(pause: Boolean){
        _isPaused.value = pause

        with(timer){
            if(pause){
                pause()
            } else {
               start()
            }
        }
    }

    class Factory(private val totalTimeToCountdownInSeconds: Long): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T
            = TimerViewModel(totalTimeToCountdownInSeconds) as T
    }


}