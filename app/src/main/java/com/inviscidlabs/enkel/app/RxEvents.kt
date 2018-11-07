package com.inviscidlabs.enkel.app

data class TimerTickRxEvent(val timerID: Int, val timeRemainingInSeconds: Long)
data class TimerExpiredEvent(val timerID: Int)

data class ResetTimerEvent(val timerID: Int)

data class HomeActivityForegroundEvent(val isRunningInForeground: Boolean)
data class NewTimerSelected(val timerID: Int)

data class PlayRequestEvent(val timerID: Int)
data class PlayPauseOutputEvent(val timerID: Int, val isPaused: Boolean)

data class RequestTimerStatusEvent(val timerID: Int)
data class ProvideTimerStatusEvent(val timerID: Int, val timeRemainingInSeconds: Long, val isPaused: Boolean)