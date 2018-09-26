package com.inviscidlabs.enkel.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class TimerEntity(
        @ColumnInfo(name = "timer_time_msecs") val timeInMS: Long,
        @ColumnInfo(name = "timer_position") val position: Int? = null,
        @PrimaryKey(autoGenerate = true) val timerID: Int? = null)