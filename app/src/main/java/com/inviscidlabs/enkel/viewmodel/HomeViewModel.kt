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

    private val TAG = this.javaClass.simpleName
    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()


//Private Mutables
    private val _selectedTimerIndex =  MutableLiveData<Int>()
    private val _timers = MutableLiveData<List<TimerEntity>>()

//Public Immutables
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

    fun timerSaved() = loadTimers()

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
                        selectNextTimer()
                        })
    }




//endregion

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
                        //TODO setInitialSelectedTimer, but better
                    })
    }

    private fun selectNextTimer(){
        //Get the current selectedIndex. If it is null return
        //get the next timer in the iteration - = list size, 0, <list size, +1, list size is 0, -1
        //Set as currently selected timer
        val currentIndex: Int = _selectedTimerIndex.value ?: return
        val listSize: Int = _timers.value?.size ?: 0
        var nextIndex = -1
        //TODO make this work with a with statement
        if((currentIndex+1) == listSize){
            nextIndex = 0
        } else if(listSize>-1 && currentIndex+1 < listSize){
            nextIndex = currentIndex+1
        }
        _selectedTimerIndex.value = nextIndex
    }

    private fun setInitialSelectedTimer() {
            _selectedTimerIndex.postValue(0)
    }

}
