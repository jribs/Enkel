package com.inviscidlabs.enkel.model.dao

import android.arch.persistence.room.*
import com.inviscidlabs.enkel.model.entity.TimerEntity

@Dao
interface TimerDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTimer(timer: TimerEntity): Long

    @Query("SELECT * FROM TimerEntity WHERE timerID = :timerID")
    fun getTimerFromID(timerID: Int): TimerEntity

    @Query("SELECT * FROM TimerEntity")
    fun getAllTimers():List<TimerEntity>

    @Delete
    fun deleteTimer(timer: TimerEntity)

    @Update
    fun updateTimer(user: TimerEntity)
}