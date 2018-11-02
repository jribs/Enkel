package com.inviscidlabs.enkel.viewmodel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.format.DateUtils
import android.util.Log
import com.inviscidlabs.enkel.EnkelTimer
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.custom.PlayRequestEvent
import com.inviscidlabs.enkel.custom.RxEventBus
import com.inviscidlabs.enkel.custom.TimerExpiredEvent
import com.inviscidlabs.enkel.model.entity.TimerEntity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

//https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice#mediastyle-notifications
//https://developer.android.com/guide/components/services#Foreground

const val ACTION_PAUSE: String = "pauseTimerInService"
const val ACTION_START_TIMER: String = "startTimerInService"
const val ACTION_RESET: String = "resetTimerInService"
const val ACTION_TIMER_UPDATED = "timerUpdated"

class EnkelTimerService: Service(){

    //Private mutables
    private val _timers = MutableLiveData<List<EnkelTimer>>()

    //Public Accessors
    val timers: LiveData<List<EnkelTimer>> get() = _timers

    val delete: LiveData<Boolean> = MutableLiveData<Boolean>()
    private val CHANNEL = "Enkel"
    private val NOTIF_ID = 6969
    private val TAG = this.javaClass.simpleName
    private val activeTimers = mutableListOf<EnkelTimer>()
    private var disposablePlayPause: Disposable? = null


//region Service Functions
    override fun onCreate() {
        listenForPauseOrPlay()
        super.onCreate()
    }


    //startID = timer unique ID
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingAction = intent?.action
        if(pendingAction!=null && intentHasNecessaryData(intent)){
            when(pendingAction){
                ACTION_START_TIMER -> startTimer(intent)
                ACTION_PAUSE -> pauseTimer(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        //We are not interested in exposing functionality to other applications right now
        //Will need when we use this as time broadcaster
        return null
    }

//endregion

//region 2nd Layer Functions
    private fun startTimer(intent: Intent) {
        if(!intentHasNecessaryData(intent)) return
        val timerID = intent.getLongExtra(getString(R.string.key_timer_id),-1L)
        val timerTime = intent.getLongExtra(getString(R.string.key_timer_time), -1L)
        val indexOfTimer = activeTimers.indexOfFirst {it.id == timerID}
        when(indexOfTimer){
            -1      -> startNewTimerWithArguments(timerTime, timerID)
            else    -> resumeTimerOfIndex(indexOfTimer)
        }
    }

    private fun listenForPauseOrPlay() {
        disposablePlayPause = RxEventBus.subscribe<PlayRequestEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {playPauseRequest ->
                    val requestedTimerIndex = activeTimers.indexOfFirst {timer->
                        timer.id == playPauseRequest.timerID.toLong()
                    }
                    val pauseTimer = playPauseRequest.isPaused
                    activeTimers[requestedTimerIndex].apply {
                        if(pauseTimer){
                            pause()
                        } else {
                            start()
                        }
                    }
                }
    }

    private fun pauseTimer(intent: Intent) {
        if(!intentHasNecessaryData(intent)) return
        val timerID = intent.getLongExtra(getString(R.string.key_timer_id),-1)
        val indexOfTimer = activeTimers.indexOfFirst { it.id ==timerID }
        when(indexOfTimer){
            -1      -> Log.e(TAG, "Couldn't find timer with ID=$timerID in list of running timers")
            else    -> pauseExistingTimer(indexOfTimer)
        }
        if(activeTimers.size<1) stopForegroundService()
    }
//endregion

//region Bottom Layer Functions
    private fun startNewTimerWithArguments(timerTime: Long, timerID: Long) {
        val timer = createTimer(timerTime, timerID)
        activeTimers.add(timer)
        activeTimers.last().start()
    }

    private fun resumeTimerOfIndex(indexOfTimerToResume: Int){
        activeTimers[indexOfTimerToResume].start()
        //TODO start ForegroundService
    }

    private fun pauseExistingTimer(indexOfTimerToPause: Int){
        val timerToPause = activeTimers[indexOfTimerToPause]
        with(timerToPause){
            pause()
            if(!hasPlayingTimers()){
               stopForeground(true)
            }
        }
    }

    private fun intentHasNecessaryData(intentToAnalyze: Intent?): Boolean {
        intentToAnalyze ?: return falseWithNullIntentMessage()
        val hasTimerTime = intentToAnalyze.hasExtra(getString(R.string.key_timer_time))
        val hasTimerID = intentToAnalyze.hasExtra(getString(R.string.key_timer_id))
        val argsAreValid = (hasTimerTime|| hasTimerID)
        return if(argsAreValid) {
            true
        } else {
            Log.e(TAG, "The timerTime or timerID are either missing or less than 1. Check supplied arguments")
            false
        }
    }
//endregion

//region Utility Functions

    private fun createTimer(timerTime: Long, timerID: Long):EnkelTimer{
        val timerID_Int = timerID.toInt()
        return object : EnkelTimer(
                countdownInterval = 1000,
                millisInFuture = 1000 * timerTime,
                id = timerID){
            override fun onFinish() {
                //TODO Notification that we are done
                RxEventBus.post(TimerExpiredEvent(timerID_Int))
                activeTimers.remove(this)
                if (activeTimers.size<1){
                    stopForegroundService()
                }
            }
            override fun onTick(millisUntilFinished: Long) {
                emitOnTick()
                startForeground(timerID_Int, notificationFromTimeRemaining(millisUntilFinished, timerID))
                sendTimeChangedBroadcast(timerID, millisUntilFinished)
            }
        }
    }


    private fun sendTimeChangedBroadcast(timerID: Long, timeUntilFinished: Long){
        val newTimeBroadcast = Intent(ACTION_TIMER_UPDATED)
        newTimeBroadcast.putExtra(getString(R.string.key_timer_time), timeUntilFinished)
        LocalBroadcastManager.getInstance(this).sendBroadcast(newTimeBroadcast)
    }

    private fun falseWithNullIntentMessage():Boolean{
        Log.e(TAG, "Supplied Intent is null. No data to start timer")
        return false
    }

    private fun hasPlayingTimers():Boolean{
        val playingTimers = activeTimers.filter {timer->
            timer.status == EnkelTimer.TimerStatus.PLAYING
        }
        return (playingTimers.isNotEmpty())
    }

    private fun deleteTimerNotification(timerID: Int) {
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.cancelAll()
    }

    private fun stopForegroundService(){
        stopForeground(true)
        stopSelf()
    }

    private fun notificationFromTimeRemaining(millisUntilFinished: Long, id: Long): Notification{
        val notifChannel = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel()
        } else {""}

        val notifBuilder = NotificationCompat.Builder(this, notifChannel)

        with(notifBuilder){
            setSmallIcon(R.drawable.play)
            setStyle(NotificationCompat.BigTextStyle())
            setContentTitle(numberOfTimersRunningText())
            setContentText("${DateUtils.formatElapsedTime(millisUntilFinished/1000)}")
            setShowWhen(false)
            setAutoCancel(false)
            setChannelId(CHANNEL)
            setSortKey(millisUntilFinished.toString())
            setGroup(id.toString())
        }
        return notifBuilder.build()
    }

    private fun numberOfTimersRunningText():String{
        val numTimersRunning = activeTimers.size
        return if(numTimersRunning<2){
            "$numTimersRunning timer running"
        }else {
            "$numTimersRunning timers running"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel():String{
            val name = "Timers"
            val description = "A channel for Enkel timers"
            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationChannel = NotificationChannel(CHANNEL, name,  importance)
            notificationChannel.description = description

            val notificationManager: NotificationManager =
                    getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
            return CHANNEL
    }
//endregion

}