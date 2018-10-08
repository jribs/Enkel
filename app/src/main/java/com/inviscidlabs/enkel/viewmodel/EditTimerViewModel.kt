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

class EditTimerViewModel(): ViewModel(){

    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()
    private val TAG = this.javaClass.simpleName

    private var insertMode = false

    //Mutable Variables
    private val _loadedTimer = MutableLiveData<TimerEntity>()
    private val _timerSavedID = MutableLiveData<Int>()

    //Immutable Properties
    val loadedTimer:LiveData<TimerEntity> get() = _loadedTimer
    val timerSavedID: LiveData<Int> get() = _timerSavedID

//UI calls
    fun communicateTimerID(timerID: Int){
        insertMode = if(timerID>-1) {
            loadTimerFromID(timerID)
            false
        } else {
            true
        }
    }

    fun saveTimerClicked(timeInMS: Long){
        if(insertMode){
            insertTimer(timeInMS = timeInMS)
        } else {
            updateTimer(timeInMS = timeInMS)
        }
    }

//2nd Layer Functions

    private fun loadTimerFromID(timerID: Int) {
        Single.fromCallable {
            _loadedTimer.postValue(timerDao.getTimerFromID(timerID))
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {
                    Log.e(TAG, "Error loading timer using Room Dao: ${it.localizedMessage}")
                })
    }

    private fun insertTimer(timeInMS: Long){
        Single.fromCallable {
        val timerToInsert = TimerEntity(timeInMS)
        _timerSavedID.postValue(timerDao.insertTimer(timerToInsert).toInt())
            //TODO delete this
            Log.e(this.javaClass.simpleName + "inserted ID", _timerSavedID.value.toString())
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(onError = {
                    Log.e(TAG, "Error Inserting timer using Room DAO: ${it.localizedMessage}")
                })
    }

    private fun updateTimer(timeInMS: Long){
        Single.fromCallable {
            val updatedTimer = _loadedTimer.value ?:
                throw RuntimeException(this.javaClass.simpleName +
                "When calling updateTimer, loadedTimer LiveData must have a value")

            updatedTimer.timeInMS = timeInMS
            timerDao.updateTimer(updatedTimer)
            _timerSavedID.postValue(updatedTimer.timerID)
            }
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onError = {
                        Log.e(TAG, "Error updating Timer with DAO: ${it.localizedMessage}")})
    }


}