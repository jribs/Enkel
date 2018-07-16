package com.inviscidlabs.enkel

import android.app.Application
import com.inviscidlabs.enkel.Dagger.AppComponent
import com.inviscidlabs.enkel.Dagger.AppModule
import com.inviscidlabs.enkel.Dagger.DaggerAppComponent

class EnkelApp: Application(){

    override fun onCreate() {
        super.onCreate()

    }

    private fun initdagger(app: EnkelApp): AppComponent =
            DaggerAppComponent.builder().appModule(AppModule(app)).build()

}