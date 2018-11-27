package com.inviscidlabs.enkel.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.util.Log
import com.inviscidlabs.enkel.app.*
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.viewmodel.service.EnkelTimerService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

//Handles all data operations and active timer changes.
// Does not maintain active timers. That is for the Service
class HomeViewModel():ViewModel(){

    private val TAG = this.javaClass.simpleName
    private val timerDao = EnkelApp.CURRENT_DB_INSTANCE.timerDao()
    private var currentTimerID: Int = 0
    private var disposableTimerStatusChange: Disposable? = null
    private var disposablePlayPauseChange: Disposable? = null

//Private Mutables
    private val _timers = MutableLiveData<List<TimerEntity>>()
    private val _targetedTimerIndexSelection = MutableLiveData<Int>()
    private val _currentTimerIsPaused = MutableLiveData<Boolean>()
    private val _initialTimerStatus = MutableLiveData<Boolean>()

//Public Accessors
    val timers: LiveData<List<TimerEntity>> get() = _timers
    val targetedTimerIndexSelection get() = _targetedTimerIndexSelection
    val currentTimerIsPaused get() = _currentTimerIsPaused
    val initialTimerStatus get() = _initialTimerStatus

    init {
        loadTimers()
        listenForInitialStatus()
        listenForPlayPauseStatus()
    }

//region UI
    fun timerSelectedFromViewPager(indexOfTimer: Int){
        currentTimerID = _timers.value?.get(indexOfTimer)?.timerID ?: -1
        emitNewTimerSelected(indexOfTimer)
    }

    fun timerSuccessfullySaved(savedTimerID: Int){
        loadTimers()
        postNewTimerIndex(savedTimerID)
    }

    fun deleteTimerClicked(){
        val timerToDelete = _timers.getIndexFromID(currentTimerID) ?: return
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

    fun resetClicked(){
        RxEventBus.post(ResetTimerEvent(currentTimerID))
    }

    fun addTimer(){

    }

    fun playPauseClicked(){
        RxEventBus.post(PlayRequestEvent(timerID = currentTimerID))
    }


//endregion

//region 2nd layer functions
    private fun loadTimers(){
        Single.fromCallable {
            _timers.postValue(timerDao.getAllTimers())

        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onError = {throwable->
                            Log.e(TAG, throwable.localizedMessage)
                        })
    }

    private fun emitNewTimerSelected(newTimerIndex: Int) {
        RxEventBus.post(NewTimerSelected(_timers.value?.get(newTimerIndex)?.timerID ?: -1))
    }

    private fun listenForInitialStatus(){
        disposableTimerStatusChange = RxEventBus.subscribe<ProvideTimerStatusEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==currentTimerID){
                        _initialTimerStatus.postValue(it.isPaused)
                    }
                }
    }

    private fun listenForPlayPauseStatus(){
        disposablePlayPauseChange = RxEventBus.subscribe<PlayPauseOutputEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID==currentTimerID){
                        _currentTimerIsPaused.postValue(it.isPaused)
                    }
                }
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
        return when (timersWithSavedID?.size) {
            0       -> false.also {Log.e(TAG, "No timers found in ViewModel List with id of $savedTimerID")}
            1       -> true
            null    -> false.also {Log.e(TAG, "filter size is null")}
            else    -> true.also {Log.e(TAG, "More than one timer found with timerID of $savedTimerID")}
        }
    }



    private fun MutableLiveData<List<TimerEntity>>.getIndexFromID(timerID: Int): TimerEntity?{
        return this.value?.get(this.value?.indexOfFirst {
            it.timerID == timerID
        } ?: -1)
    }
//endregion


    //region Utility Functions
    private fun startNewTimerInService(isPaused: Boolean){
        val playPauseIntent = Intent(app.applicationContext, EnkelTimerService::class.java).apply {
            putExtra(INTENT_TIMERID, currentTimerID.toLong())
            putExtra(INTENT_TIMERTIME, secondsToCountdown)
            action = ACTION_START_TIMER
        }
        app.applicationContext.startService(playPauseIntent)
    }
}
