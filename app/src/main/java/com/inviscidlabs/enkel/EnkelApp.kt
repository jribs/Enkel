package com.inviscidlabs.enkel

import android.app.Application
import android.arch.persistence.room.Room
import com.inviscidlabs.enkel.model.EnkelDatabase

class EnkelApp: Application(){

    override fun onCreate() {
        super.onCreate()
        CURRENT_DB_INSTANCE =
                Room.databaseBuilder(this, EnkelDatabase::class.java, "Enkel_Database").build()
    }


    companion object {
        lateinit var CURRENT_DB_INSTANCE: EnkelDatabase
    }
}