package com.inviscidlabs.enkel

import android.os.CountDownTimer

abstract class EnkelTimer(private val millisInFuture: Long, private val countdownInterval: Long, val id: Long){

    private var timeLeftInMillis = millisInFuture
    private var timer: CountDownTimer? = null
    //TODO use for sorting timers val timeLeftInMilliseconds get() = timeLeftInMillis

    private fun startTimerFromTimeLeft(){
        timer = object: CountDownTimer(timeLeftInMillis, countdownInterval){
            override fun onFinish() {
                this@EnkelTimer.onFinish()
            }
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                this@EnkelTimer.onTick(millisUntilFinished)
            }
        }.start()
    }

    fun start(){
        startTimerFromTimeLeft()
    }

    fun pause(){
        timer?.cancel()
    }

    //reset to initial time and broadcast new time without starting timer
    fun reset(){
        timer?.cancel()
        timeLeftInMillis = millisInFuture
        onTick(timeLeftInMillis)
    }

    abstract fun onFinish()
    abstract fun onTick(millisUntilFinished: Long)
}