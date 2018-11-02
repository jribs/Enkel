package com.inviscidlabs.enkel.ui.custom

import android.support.v4.view.ViewPager
import com.inviscidlabs.enkel.viewmodel.HomeViewModel

class SelectedTimerChangedListener(private val homeViewModel: HomeViewModel): ViewPager.OnPageChangeListener {
    //TODO figure out what these two Scrolly Bois do
    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        homeViewModel.timerSelectedFromViewPager(currentPosition = position)
        //TODO delete
    }
}

