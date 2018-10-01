package com.inviscidlabs.enkel.ui.custom

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import com.inviscidlabs.enkel.ui.NoDataFragment
import com.inviscidlabs.enkel.ui.TimerFragment
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlin.RuntimeException

class TimerSlidePagerAdapter(fragmentManager: FragmentManager, private val viewModel: HomeViewModel):
        FragmentStatePagerAdapter(fragmentManager){

    private val TAG = this.javaClass.simpleName

    override fun getItem(position: Int): Fragment {
        if (count > 0) {
            return makeTimerFragment(position)
        }
        return NoDataFragment()
    }


    override fun getCount(): Int = viewModel.timers.value?.size ?: 0

    override fun notifyDataSetChanged() {

        super.notifyDataSetChanged()
    }

    //Internal Functions
    private fun makeTimerFragment(position: Int): Fragment {
        val timeInMS = viewModel.timers.value?.get(position)?.timeInMS ?: 0
        return if (timeInMS > 0) {
            TimerFragment.newInstance(timeInMS)
        } else {
            Log.e(TAG, "Stored time for item in $position is either null or 0")
            NoDataFragment()
        }
    }
}