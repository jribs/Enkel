package com.inviscidlabs.enkel

import android.os.CountDownTimer

abstract class EnkelTimer(private val millisInFuture: Long, private val countdownInterval: Long){

    private var timeLeftInMillis = millisInFuture
    var hasStarted = false
        private set

    private var timer = object: CountDownTimer(timeLeftInMillis, countdownInterval){
        override fun onFinish() {
            this@EnkelTimer.onFinish()
        }
        override fun onTick(millisUntilFinished: Long) {
            timeLeftInMillis = millisUntilFinished
            this@EnkelTimer.onTick(millisUntilFinished)
        }
    }

    fun start(){
        timer.start()
        hasStarted=true
    }

    fun pause(){
        timer.cancel()
    }

    fun resume(){
        timer.start()
    }

    abstract fun onFinish()
    abstract fun onTick(millisUntilFinished: Long)

}