package com.inviscidlabs.enkel.viewmodel

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.inviscidlabs.enkel.EnkelApp
import com.inviscidlabs.enkel.custom.NewTimerSelected
import com.inviscidlabs.enkel.custom.RxEventBus
import com.inviscidlabs.enkel.model.entity.TimerEntity
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

//Handles all data operations. Does not maintain active timers. That is for the Service
class HomeViewModel():ViewModel(){

    private val TAG = this.javaClass.simpleName
    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()

//Private Mutables
    private val _selectedTimerIndex =  MutableLiveData<Int>()
    private val _timers = MutableLiveData<List<TimerEntity>>()
    private val _targetedTimerIndexSelection = MutableLiveData<Int>()

//Public Accessors
    val selectedTimerIndex: LiveData<Int> get() = _selectedTimerIndex
    val timers: LiveData<List<TimerEntity>> get() = _timers
    val targetedTimerIndexSelection get() = _targetedTimerIndexSelection

    init {
        loadTimers()
        setInitialSelectedTimer()
    }

//region UI
    fun timerSelectedFromViewPager(indexOfTimer: Int){
        _selectedTimerIndex.value = indexOfTimer
        emitNewTimerSelected(indexOfTimer)
    }

    fun timerSuccessfullySaved(savedTimerID: Int){
        loadTimers()
        postNewTimerIndex(savedTimerID)
    }

    @SuppressLint("CheckResult")
    fun deleteTimerClicked(){
        val currentIndex = _selectedTimerIndex.value ?: return
        val timerToDelete = _timers.value?.get(currentIndex) ?: return

        Single.fromCallable {
            timerDao.deleteTimer(timerToDelete)
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                    onError = {throwable ->
                        Log.e(TAG, throwable.localizedMessage)
                },
                    onSuccess = {
                        Log.e(TAG, "$it rows successfully deleted")
                        loadTimers()
                        })
    }
//endregion

//region 2nd layer functions
    @SuppressLint("CheckResult")
    private fun loadTimers(){
        Single.fromCallable {
            _timers.postValue(timerDao.getAllTimers())

        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onError = {throwable->
                            Log.e(TAG, throwable.localizedMessage)
                        },
                        onSuccess = {
                        })
    }

    private fun emitNewTimerSelected(newTimerIndex: Int) {
        RxEventBus.post(NewTimerSelected(_timers.value?.get(newTimerIndex)?.timerID ?: -1))
    }

    private fun postNewTimerIndex(savedTimerID: Int) {
        Single.fromCallable {
            if (isValidTimerID(savedTimerID)) {
                val newTimerIndex = _timers.value?.indexOfFirst { timerWithID ->
                    timerWithID.timerID == savedTimerID
                } ?: return@fromCallable

                _targetedTimerIndexSelection.postValue(newTimerIndex)
            }
        }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun isValidTimerID(savedTimerID: Int):Boolean {
        val timersWithSavedID = timers.value?.filter { it.timerID == savedTimerID }
        when (timersWithSavedID?.size) {
            0       -> return false.also {Log.e(TAG, "No timers found in ViewModel List with id of $savedTimerID")}
            1       -> return true
            null    -> return false.also {Log.e(TAG, "filter size is null")}
            else    -> return true.also {Log.e(TAG, "More than one timer found with timerID of $savedTimerID")}
        }
        return true
    }

    private fun setInitialSelectedTimer() {
            _selectedTimerIndex.postValue(0)
    }

//endregion

}
