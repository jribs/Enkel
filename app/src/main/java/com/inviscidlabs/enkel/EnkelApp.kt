package com.inviscidlabs.enkel

import android.app.Application
import com.inviscidlabs.enkel.Dagger.AppComponent
import com.inviscidlabs.enkel.Dagger.AppModule
import com.inviscidlabs.enkel.Dagger.DaggerAppComponent

class EnkelApp: Application(){

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = makeAppComponent(this)
    }

    private fun makeAppComponent(app: EnkelApp): AppComponent =
            DaggerAppComponent.builder().appModule(AppModule(app)).build()
}