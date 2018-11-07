package com.inviscidlabs.enkel

import android.os.CountDownTimer

abstract class EnkelTimer(private val millisInFuture: Long, private val countdownInterval: Long, val id: Long){

    private var _timeLeftInMillis = millisInFuture
    private var timer: CountDownTimer? = null
    private var _status: TimerStatus = TimerStatus.PAUSED
    //TODO use for sorting timers val timeLeftInMilliseconds get() = _timeLeftInMillis

    val isPaused get() = (status != TimerStatus.PLAYING)
    val status get() = _status
    val timeLeftInMillis get() = _timeLeftInMillis

    private fun startTimerFromTimeLeft(){
        timer = object: CountDownTimer(_timeLeftInMillis, countdownInterval){
            override fun onFinish() {

                this@EnkelTimer.onFinish()

            }
            override fun onTick(millisUntilFinished: Long) {
                if(timeLeftInMillis<=0L){ status==TimerStatus.FINISHED}
                _timeLeftInMillis = millisUntilFinished
                this@EnkelTimer.onTick(millisUntilFinished)
            }
        }.start()
    }

    fun start(){
        startTimerFromTimeLeft()
        _status = TimerStatus.PLAYING
    }

    fun pause(){
        _status = TimerStatus.PAUSED
        timer?.cancel()
    }

    //reset to initial time and broadcast new time without starting timer
    fun reset(){
        timer?.cancel()
        _timeLeftInMillis = millisInFuture
        onTick(_timeLeftInMillis)
        _status = TimerStatus.PAUSED
    }

    abstract fun onFinish()
    abstract fun onTick(millisUntilFinished: Long)

    enum class TimerStatus{
        PLAYING, PAUSED, FINISHED
    }
}