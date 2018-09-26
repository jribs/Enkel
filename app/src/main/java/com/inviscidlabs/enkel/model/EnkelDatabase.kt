package com.inviscidlabs.enkel.model

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.inviscidlabs.enkel.model.dao.TimerDao
import com.inviscidlabs.enkel.model.entity.TimerEntity


@Database(entities = [TimerEntity::class], version = 1)
abstract class EnkelDatabase: RoomDatabase(){
    abstract fun timerDao(): TimerDao
}