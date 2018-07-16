package com.inviscidlabs.enkel.Dagger

import com.inviscidlabs.enkel.EnkelTimer
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent{

    fun injectFragment(enkelTimerFragment: EnkelTimer)
}