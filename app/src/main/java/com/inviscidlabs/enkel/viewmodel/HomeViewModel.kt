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

    private val _selectedTimer =  MutableLiveData<TimerEntity>()
    private val _timers = MutableLiveData<List<TimerEntity>>()
    private val _loading = MutableLiveData<Boolean>()

    val selectedTimer: LiveData<TimerEntity> get() = _selectedTimer
    val timers: LiveData<List<TimerEntity>> get() = _timers
    val loading: LiveData<Boolean> get() = _loading

    fun insertTimer(timer: TimerEntity){
        timerDao.insertTimer(timer=timer)
    }

    init {
        loadTimers()
    }

    fun onSwipeUp(){}
    fun onSwipeDown(){}


    private fun loadTimers(){
        Single.fromCallable {
            _loading.postValue(true)
            _timers.postValue(timerDao.getAllTimers())
            _loading.postValue(false)
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {throwable->
                    _loading.postValue(false)
                    Log.e(this.javaClass.simpleName, throwable.localizedMessage)
                })
    }

}
