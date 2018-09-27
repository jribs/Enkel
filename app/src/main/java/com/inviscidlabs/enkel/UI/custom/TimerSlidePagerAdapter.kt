package com.inviscidlabs.enkel.ui.custom

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.inviscidlabs.enkel.ui.TimerFragment
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlin.RuntimeException

class TimerSlidePagerAdapter(val fragmentManager: FragmentManager, val viewModel: HomeViewModel,
                             val fragTagPrefix: String):
        FragmentStatePagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment {
        //Get the fragment by tag. If it is null, make a new one
        val fragmentAtPosition =fragmentManager.findFragmentByTag("$fragTagPrefix$position")

        if(fragmentAtPosition==null){
            val timerList = viewModel.timers.value ?: throw RuntimeException(
                    "${this.javaClass.simpleName}: timerList is null"
            )
            val timeInMS = timerList[position].timeInMS
            TimerFragment.newInstance(timeInMS)
        } else {
            return fragmentAtPosition
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCount(): Int {
        return viewModel.timers.value?.size ?: 0
    }



}