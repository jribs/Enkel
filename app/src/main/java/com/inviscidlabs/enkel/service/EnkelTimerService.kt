package com.inviscidlabs.enkel.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.inviscidlabs.enkel.EnkelTimer
import com.inviscidlabs.enkel.R

//https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice#mediastyle-notifications
//https://developer.android.com/guide/components/services#Foreground

class EnkelTimerService: Service(){

    private val TAG = this.javaClass.simpleName
    private val activeTimers = mutableListOf<EnkelTimer>()

    //startID = timer unique ID
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intentHasNecessaryData(intent)){
            startTimerWithArguments(intent!!)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        //We are not interested in exposing functionality to other applications
        return null
    }


    private fun startTimerWithArguments(intent: Intent) {
        val timerTime =intent.getLongExtra(getString(R.string.key_timer_time), -1L)
        val timerID = intent.getLongExtra(getString(R.string.key_timer_id), -1L)
        val timer = makeTimer(timerTime, timerID)
        activeTimers.add(timer)
    }


    private fun intentHasNecessaryData(intentToAnalyze: Intent?): Boolean {
        intentToAnalyze ?: return intentIsNull()
        val timerTime = intentToAnalyze.getLongExtra(getString(R.string.key_timer_time), -1L)
        val timerID = intentToAnalyze.getLongExtra(getString(R.string.key_timer_id), -1L)
        return necessaryTimerArgumentsAreValid(timerTime, timerID)
    }


//region Bottom Layer Functions
    private fun intentIsNull():Boolean{
        Log.e(TAG, "Supplied Intent is null. No data to start timer")
        return false
    }

    private fun necessaryTimerArgumentsAreValid(timerTime: Long, timerID: Long):Boolean{
        val argsAreValid = !(timerTime<1 || timerID<1)
        if(argsAreValid) {
            return true
        } else {
            Log.e(TAG, "The timerTime or timerID are either missing or less than 1. Check supplied arguments")
            return false
        }
    }

    private fun makeTimer(timerTime: Long, timerID: Long):EnkelTimer{
        return object : EnkelTimer(
                countdownInterval = 1000,
                millisInFuture = 1000 * timerTime,
                id = timerID){
            override fun onFinish() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTick(millisUntilFinished: Long) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }
//endregion

}