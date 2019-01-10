package com.inviscidlabs.enkel.app

import android.app.Application
import android.arch.persistence.room.Room
import android.util.Log
import com.inviscidlabs.enkel.model.EnkelDatabase
import io.reactivex.plugins.RxJavaPlugins

class EnkelApp: Application(){



    override fun onCreate() {
        super.onCreate()

        RxJavaPlugins.setErrorHandler { t: Throwable? ->
            Log.e(this.javaClass.simpleName, t?.message)
        }

        CURRENT_DB_INSTANCE =
                Room.databaseBuilder(this, EnkelDatabase::class.java, "Enkel_Database").build()
    }


    companion object {
        lateinit var CURRENT_DB_INSTANCE: EnkelDatabase
        val rxBus = RxEventBus()
    }
}