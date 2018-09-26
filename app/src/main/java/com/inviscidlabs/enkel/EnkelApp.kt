package com.inviscidlabs.enkel

import android.app.Application
import android.arch.persistence.room.Room
import com.inviscidlabs.enkel.Dagger.AppComponent
import com.inviscidlabs.enkel.Dagger.AppModule
import com.inviscidlabs.enkel.Dagger.DaggerAppComponent
import com.inviscidlabs.enkel.model.EnkelDatabase

class EnkelApp: Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = makeAppComponent(this)
        CURRENT_DB_INSTANCE =
                Room.databaseBuilder(this, EnkelDatabase::class.java, "Enkel_Database").build()
    }

    private fun makeAppComponent(app: EnkelApp): AppComponent =
            DaggerAppComponent.builder().appModule(AppModule(app)).build()

    companion object {
        lateinit var CURRENT_DB_INSTANCE: EnkelDatabase
    }
}