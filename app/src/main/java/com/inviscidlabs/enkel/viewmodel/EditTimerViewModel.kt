package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.model.entity.TimerEntity
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

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
        Single.fromCallable {
            _loadedTimer.postValue(timerDao.getTimerFromID(timerID))
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {
                    Log.e(this.javaClass.simpleName, "Error loading timer using Room Dao")
                })
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
        Single.fromCallable {
        val timerToInsert = TimerEntity(timeInMS)
        _timerSavedID.postValue(timerDao.insertTimer(timerToInsert))
            //TODO delete this
            Log.e(this.javaClass.simpleName + "inserted ID", _timerSavedID.value.toString())
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {
                    Log.e(this.javaClass.simpleName, "Error Inserting timer using Room DAO: ${it.localizedMessage}")
                })
    }

    private fun updateTimer(timeInMS: Long){
        Single.fromCallable {
            val updatedTimer = _loadedTimer.value ?:
                throw RuntimeException(this.javaClass.simpleName +
                "When calling updateTimer, loadedTimer LiveData must have a value")

            updatedTimer.timeInMS = timeInMS
            timerDao.updateTimer(updatedTimer)
            _timerSavedID.postValue(updatedTimer.timerID?.toLong())
            }
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onError = {
                        Log.e(this.javaClass.simpleName, "Error updating Timer with DAO")})
    }


}