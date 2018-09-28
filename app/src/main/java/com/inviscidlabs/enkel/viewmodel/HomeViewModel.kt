package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.model.entity.TimerEntity
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class HomeViewModel():ViewModel(){

    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()

    private val _selectedTimerIndex =  MutableLiveData<Int>()
    private val _timers = MutableLiveData<List<TimerEntity>>()

    val selectedTimerIndex: LiveData<Int> get() = _selectedTimerIndex
    val timers: LiveData<List<TimerEntity>> get() = _timers


    fun insertTimer(timer: TimerEntity){
        timerDao.insertTimer(timer=timer)
    }

    init {
        loadTimers()
        setInitialSelectedTimer()
    }

//region UI
    fun onSwipeUp(){
        with(_selectedTimerIndex.value ?: return){
            _selectedTimerIndex.postValue(this.inc())
        }

    }

    fun onSwipeDown(){
        with(_selectedTimerIndex.value ?: return){
            _selectedTimerIndex.postValue(this.dec())
        }
    }
    private fun loadTimers(){
        Single.fromCallable {
            _timers.postValue(timerDao.getAllTimers())

        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {throwable->
                    Log.e(this.javaClass.simpleName, throwable.localizedMessage)
                })
    }

//endregion


    private fun setInitialSelectedTimer() {
            _selectedTimerIndex.postValue(0)

    }

}