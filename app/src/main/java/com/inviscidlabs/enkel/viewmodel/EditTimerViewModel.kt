package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.model.entity.TimerEntity

class EditTimerViewModel(): ViewModel(){

    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()

    //Mutable Variables
    private val _insertMode = MutableLiveData<Boolean>()
    private val _loadedTimer = MutableLiveData<TimerEntity>()
    private val _savingTimer = MutableLiveData<Boolean>()
    private val _timerSavedID = MutableLiveData<Long>()
    //Immutable Properties
    public val insertMode:LiveData<Boolean> get() = _insertMode
    public val loadedTimer:LiveData<TimerEntity> get() = _loadedTimer
    public val savingTimer: LiveData<Boolean> get() = _savingTimer
    public val timerSavedID: LiveData<Long> get() = _timerSavedID

//UI calls
    fun loadTimer(timerID: Int){
        _loadedTimer.value = timerDao.getTimerFromID(timerID)
    }

    fun saveTimer(timeInMS: Long){
        insertMode.value?.let {insertModeValue ->
            if(insertModeValue){
                insertTimer(timeInMS = timeInMS)
            } else {
                updateTimer(timeInMS = timeInMS)
            }
        }
    }

    fun setInsertMode(insertModeValue: Boolean){
        _insertMode.value = insertModeValue
    }

//2nd Layer Functions

    private fun insertTimer(timeInMS: Long){
        val timerToInsert = TimerEntity(timeInMS)
        _timerSavedID.value = timerDao.insertTimer(timerToInsert)
    }

    private fun updateTimer(timeInMS: Long){
        val updatedTimer = _loadedTimer.value ?: throw RuntimeException(this.javaClass.simpleName +
                        "When calling updateTimer, loadedTimer LiveData must have a value")
        updatedTimer.timeInMS = timeInMS
        timerDao.updateUser(updatedTimer)
        _timerSavedID.value = updatedTimer.timerID?.toLong() ?: throw RuntimeException(this.javaClass.simpleName +
                            "loaded and updated timer must have a valid ID")
    }



}