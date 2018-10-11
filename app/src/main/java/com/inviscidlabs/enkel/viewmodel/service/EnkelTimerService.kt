package com.inviscidlabs.enkel.viewmodel.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.text.format.DateUtils
import android.util.Log
import com.inviscidlabs.enkel.EnkelTimer
import com.inviscidlabs.enkel.R
import java.util.*

//https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice#mediastyle-notifications
//https://developer.android.com/guide/components/services#Foreground

const val ACTION_PAUSE: String = "pauseTimerInService"
const val ACTION_START_TIMER: String = "startTimerInService"
const val ACTION_RESET: String = "resetTimerInService"

class EnkelTimerService: Service(){

    private val CHANNEL = "Enkel"
    private val TAG = this.javaClass.simpleName
    private val activeTimers = mutableListOf<EnkelTimer>()

//region Service Functions
    override fun onCreate() {
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
        val timerID = intent.getLongExtra(getString(R.string.key_timer_id),-1)
        val timerTime = intent.getLongExtra(getString(R.string.key_timer_time), -1L)
        val indexOfTimer = activeTimers.indexOfFirst {it.id == timerID}
        when(indexOfTimer){
            -1      -> startNewTimerWithArguments(timerTime, timerID)
            else    -> resumeTimerOfIndex(indexOfTimer)
        }
    }

    private fun pauseTimer(intent: Intent) {
        if(!intentHasNecessaryData(intent)) return
        val timerID = intent.getLongExtra(getString(R.string.key_timer_id),-1)
        val indexOfTimer = activeTimers.indexOfFirst { it.id ==timerID }
        when(indexOfTimer){
            -1      -> pauseExistingTimer(indexOfTimer)
            else    -> Log.e(TAG, "Couldn't find timer with ID=$timerID in list of running timers")
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
        activeTimers[indexOfTimerToPause].pause()
        //TODO stop foreground if no other active services
    }

    private fun intentHasNecessaryData(intentToAnalyze: Intent?): Boolean {
        intentToAnalyze ?: return intentIsNull()
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

    private fun createTimer(timerTime: Long, timerID: Long):EnkelTimer{
        val timerID_Int = timerID.toInt()
        return object : EnkelTimer(
                countdownInterval = 1000,
                millisInFuture = 1000 * timerTime,
                id = timerID){
            override fun onFinish() {
                //TODO Notification that we are done
                activeTimers.remove(this)
                if (activeTimers.size<2){
                    stopForegroundService()
                }
            }

            override fun onTick(millisUntilFinished: Long) {

                startForeground(timerID_Int, notificationFromTimeRemaining(millisUntilFinished))
            }
        }
    }

//endregion

//region Utility Functions


    private fun intentIsNull():Boolean{
        Log.e(TAG, "Supplied Intent is null. No data to start timer")
        return false
    }

    private fun stopForegroundService(){
        //TODO broadcast time on stop
        stopForeground(true)
        stopSelf()
    }

    private fun notificationFromTimeRemaining(millisUntilFinished: Long): Notification{
        val notifChannel = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel()
        } else {""}

        val notifBuilder = NotificationCompat.Builder(this, notifChannel)
        with(notifBuilder){
            setSmallIcon(R.drawable.play)
            setStyle(NotificationCompat.BigTextStyle())
            setContentTitle("${activeTimers.size} timers running")
            setContentText("${DateUtils.formatElapsedTime(millisUntilFinished/1000)}")
            setShowWhen(false)
            setAutoCancel(false)
        }
        return notifBuilder.build()
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