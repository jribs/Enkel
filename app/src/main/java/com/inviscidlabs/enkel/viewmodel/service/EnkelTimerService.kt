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
import com.inviscidlabs.enkel.app.EnkelTimer
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.app.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

//https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice#mediastyle-notifications
//https://developer.android.com/guide/components/services#Foreground



class EnkelTimerService: Service(){

    private val CHANNEL = "Enkel"
    private val NOTIF_ID = 6969
    private val TAG = this.javaClass.simpleName
    private val GROUP_TICKING_TIMERS = "com.inviscidlabs.enkel.TICKING_TIMERS"

    private val activeTimers = mutableListOf<EnkelTimer>()
    private var isInForegroundHomeActivity = true

    private var disposablePlayPause: Disposable? = null
    private var disposableReset: Disposable? = null
    private var disposableStatusRequest: Disposable? = null
    private var disposableHomeActivityChange: Disposable? = null
    private var disposableNewSelectedTimer: Disposable? = null

//region Service Functions
    override fun onCreate() {
        listenForPauseOrPlay()
        listenForReset()
        listenForTimerStatusRequest()
        listenForHomeActivityStateChange()
        listenForNewSelectedTimer()
        super.onCreate()
    }

    //startID = timer unique ID
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingAction = intent?.action
        if(pendingAction!=null && intentHasNecessaryData(intent)){
            when(pendingAction){
                ACTION_START_TIMER -> startTimer(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        //We are not interested in exposing functionality to other applications right now
        return null
    }

//endregion

//region 2nd Layer Functions
    private fun startTimer(intent: Intent) {
        if(!intentHasNecessaryData(intent)) return
        val timerID = intent.getLongExtra(INTENT_TIMERID,-1L)
        val timerTime = intent.getLongExtra(INTENT_TIMERTIME, -1L)
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
                    activeTimers[requestedTimerIndex].apply {
                        if(!isPaused){
                            pause()
                            assessLifeCycle()
                        } else {
                            start()
                        }
                        RxEventBus.post(PlayPauseOutputEvent(id.toInt(),isPaused))
                    }
                }
    }

    private fun listenForReset() {
        disposableReset = RxEventBus.subscribe<ResetTimerEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{event ->
                    val resetTimerIndex = activeTimers.indexOfFirst {it.id ==event.timerID.toLong()}
                    activeTimers[resetTimerIndex].reset()
                    RxEventBus.post(PlayPauseOutputEvent(event.timerID, true))
                }
    }

    private fun listenForTimerStatusRequest(){
        disposableStatusRequest = RxEventBus.subscribe<RequestTimerStatusEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { event ->
                    val requestedTimerIndex = activeTimers.indexOfFirst { it.id == event.timerID.toLong() }
                        if(requestedTimerIndex>-1) {
                            with(activeTimers[requestedTimerIndex]) {
                                val isPaused = (status== EnkelTimer.TimerStatus.PAUSED)
                                RxEventBus.post(
                                        ProvideTimerStatusEvent(id.toInt(), timeLeftInMillis, isPaused))
                            }
                        }
                }
        }

    private fun listenForHomeActivityStateChange(){
        disposableHomeActivityChange = RxEventBus.subscribe<HomeActivityForegroundEvent>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.isRunningInForeground){
                        stopForeground(true)
                    } else {
                        startForeground(NOTIF_ID, notificationFromRunningTimers())
                    }
                    isInForegroundHomeActivity = it.isRunningInForeground
                }
    }

    private fun listenForNewSelectedTimer() {
        disposableNewSelectedTimer = RxEventBus.subscribe<NewTimerSelected>()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.timerID>-1){
                        emitTimerStatusOfId(it.timerID)
                    } else {
                        Log.e(TAG, "Newly selected TimerID is is invalid. ID = ${it.timerID}")
                    }
                }
    }

//endregion

//region Bottom Layer Functions
    private fun startNewTimerWithArguments(timerTime: Long, timerID: Long) {
        val timer = createTimer(timerTime, timerID)
        with(activeTimers){
            add(timer)
            get(indexOfLast { it.id == timerID }).start()
            RxEventBus.post(PlayPauseOutputEvent(timerID.toInt(), false))
        }
    }

    private fun resumeTimerOfIndex(indexOfTimerToResume: Int){
        activeTimers[indexOfTimerToResume].start()
    }

    private fun intentHasNecessaryData(intentToAnalyze: Intent?): Boolean {
        intentToAnalyze ?: return false.also {
            Log.e(TAG, "Supplied Intent is null. No data to start timer")}

        val hasTimerTime = intentToAnalyze.hasExtra(INTENT_TIMERTIME)
        val hasTimerID = intentToAnalyze.hasExtra(INTENT_TIMERID)
        val argsAreValid = (hasTimerTime && hasTimerID)
        return if(argsAreValid) {
            true
        } else {
            Log.e(TAG, "The timerTime or timerID are either missing or less than 1. Check supplied arguments")
            false
        }
    }
//endregion

//region Utility Functions

    private fun createTimer(timerTime: Long, timerID: Long): EnkelTimer {
        val timerID_Int = timerID.toInt()
        return object : EnkelTimer(
                countdownInterval = 1000,
                millisInFuture = 1000 * timerTime,
                id = timerID){
            override fun onFinish() {
                //TODO Notification that we are done
                RxEventBus.post(TimerExpiredEvent(timerID_Int))
                notifyTimerDone(timerTime)
                activeTimers.remove(this)
                assessLifeCycle()
            }
            override fun onTick(millisUntilFinished: Long) {
                RxEventBus.post(TimerTickRxEvent(timerID_Int, millisUntilFinished))
                if(!isInForegroundHomeActivity){
                    startForeground(NOTIF_ID,
                            notificationFromRunningTimers())
                }
            }
        }
    }

    private fun emitTimerStatusOfId(timerID: Int) {
        val timerFromArgs = activeTimers.getTimerWithID(timerID)
        if(timerFromArgs!=null) {
            RxEventBus.post(ProvideTimerStatusEvent(timerID, timerFromArgs.timeLeftInMillis, timerFromArgs.isPaused))
        } else {
            Log.e(TAG, "Couldn't find timer of ID $timerID in active timer list")
        }
    }

    private fun assessLifeCycle() {
        if(activeTimers.size<=0) stopSelf()
        if(activeTimers.all {it.isPaused }){
            if(!isInForegroundHomeActivity){
                //TODO update paused times in RoomDB
                stopSelf()
            }
        }
    }

    private fun List<EnkelTimer>.getTimerWithID(timerID: Int): EnkelTimer?{
        return this.firstOrNull{ it.id.toInt() == timerID }
    }

    private fun notificationFromRunningTimers(): Notification{
        val notifChannel = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createNotificationChannel()
        } else {""}

        val timerUpdateNotification = NotificationCompat.Builder(this, notifChannel)
        val playingTimers = activeTimers.filter {
            it.status == EnkelTimer.TimerStatus.PLAYING
        }
        val inboxStyle = NotificationCompat.InboxStyle()
        playingTimers.forEach {
            inboxStyle.addLine("${DateUtils.formatElapsedTime(it.timeLeftInMillis/1000)}")
        }
        val minTime = (playingTimers.minBy {
            it.timeLeftInMillis }?.timeLeftInMillis ?: 0)/1000
        inboxStyle.setSummaryText(numberOfTimersRunningText())


        with(timerUpdateNotification){
            setGroup(GROUP_TICKING_TIMERS)
            setContentText("${DateUtils.formatElapsedTime(minTime)} ${numberOfOtherTimersRunning()}")
            setSmallIcon(R.drawable.play)
            setStyle(inboxStyle)
            setShowWhen(false)
            setAutoCancel(false)
            setChannelId(CHANNEL)
        }
        return timerUpdateNotification.build()
    }

    //TODO use
    private fun notifyTimerDone(totalTime: Long) {
        val mBuilder = NotificationCompat.Builder(this, CHANNEL)
        with(mBuilder) {
            setSmallIcon(R.drawable.play)
            setContentTitle("Enkel TimerEntity Done")
            setContentText("$totalTime seconds are up")
            setStyle(NotificationCompat.BigTextStyle().bigText("${totalTime} seconds is up"))
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
        }
    }

        private fun numberOfTimersRunningText():String{
        val numTimersRunning = activeTimers.size
        return when{
            numTimersRunning == 1   -> "$numTimersRunning ${getString(R.string.notif_timer_running)}"
            numTimersRunning>1      ->"$numTimersRunning ${getString(R.string.notif_timers_running)}"
            else                    ->""
        }
    }

    private fun numberOfOtherTimersRunning(): String{
        val numOtherTimers = activeTimers.size-1
        return when{
            (numOtherTimers>1)  -> "+ ${numOtherTimers} ${getString(R.string.notif_otherTimers)}"
            (numOtherTimers==1) -> "+ ${numOtherTimers} ${getString(R.string.notif_otherTimer)}"
            else                -> ""
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