package com.inviscidlabs.enkel.custom

data class TimerTickRxEvent(val timerID: Int, val timeRemainingInSeconds: Long)
data class TimerExpiredEvent(val timerID: Int)
data class PlayRequestEvent(val timerID: Int, val isPaused: Boolean)