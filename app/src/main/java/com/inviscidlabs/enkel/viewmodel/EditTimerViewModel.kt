package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.model.entity.TimerEntity
import java.util.*
import kotlin.concurrent.timer

class EditTimerViewModel(): ViewModel(){

    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()

    //Mutable Variables
    private val _insertMode = MutableLiveData<Boolean>()
    private val _selectedTimer = MutableLiveData<TimerEntity>()
    //Immutable Properties
    public val insertMode get() = _insertMode
    public val selectedTimer get() = _selectedTimer


    fun loadTimer(){}

    fun saveTimer(timeInMS: Long){
        insertMode.value?.let {insertModeValue ->
            if(insertModeValue){
                val timerToInsert = TimerEntity(timeInMS)
                timerDao.insertTimer(timerToInsert)
            } else {
                val updatedTimer = _selectedTimer.value ?: return@let
                timerDao.updateUser(updatedTimer)
            }
        }
    }

    fun setInsertMode(insertModeValue: Boolean){
        _insertMode.value = insertModeValue
    }




}