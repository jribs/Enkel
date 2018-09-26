package com.inviscidlabs.enkel.model.dao

import android.arch.persistence.room.*
import com.inviscidlabs.enkel.model.entity.TimerEntity

@Dao
interface TimerDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTimer(timer: TimerEntity): Long

    @Query("SELECT * FROM TimerEntity WHERE timerID = :timerID")
    fun getTimerFromID(timerID: Int): TimerEntity

    @Delete
    fun deleteUser(timer: TimerEntity)

    @Update
    fun updateUser(user: TimerEntity)
}