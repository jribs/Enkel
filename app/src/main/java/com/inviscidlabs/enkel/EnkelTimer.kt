package com.inviscidlabs.enkel

import android.os.CountDownTimer

abstract class EnkelTimer(private val millisInFuture: Long, private val countdownInterval: Long){

    private var timeLeftInMillis = millisInFuture


    private var timer: CountDownTimer? = null

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


    abstract fun onFinish()
    abstract fun onTick(millisUntilFinished: Long)

}