package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.model.entity.TimerEntity


class HomeViewModel():ViewModel(){

    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()

    private val _selectedTimer =  MutableLiveData<TimerEntity>()
    private val _timers = MutableLiveData<List<TimerEntity>>()

    val selectedTimer: LiveData<TimerEntity> get() = _selectedTimer
    val timers: LiveData<List<TimerEntity>> get() = _timers

    fun insertTimer(timer: TimerEntity){
        timerDao.insertTimer(timer=timer)
    }



}
