package com.inviscidlabs.enkel.Dagger

import com.inviscidlabs.enkel.EnkelTimer
import com.inviscidlabs.enkel.UI.TimerFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent{

    fun injectFragment(enkelTimerFragment: TimerFragment)
}