package com.inviscidlabs.enkel.ui.custom

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.ui.home.NoDataFragment
import com.inviscidlabs.enkel.ui.home.TimerFragment

class TimerSlidePagerAdapter(fragmentManager: FragmentManager, private val timers: List<TimerEntity>?):
        FragmentStatePagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment {
        if (count > 0) {
            return makeTimerFragment(position)
        }
        return NoDataFragment()
    }

    override fun getCount(): Int = timers?.size ?: 0

    //Internal Functions
    private fun makeTimerFragment(position: Int): Fragment {
        timers ?: return NoDataFragment()
        val timeInMS = timers[position].timeInMS
        return if (timeInMS > 0) {
            TimerFragment.newInstance(timeInMS)
        } else {
            NoDataFragment()
        }
    }
}