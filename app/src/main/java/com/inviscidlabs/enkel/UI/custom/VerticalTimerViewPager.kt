package com.inviscidlabs.enkel.ui.custom

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

//Reference is : https://stackoverflow.com/questions/13477820/android-vertical-viewpager

//Overriding default touch events and swapping x/y coordinates prior to handling
class VerticalTimerViewPager(context: Context, attributeSet: AttributeSet): ViewPager(context, attributeSet){

    var timerViewPagerEventListener: TimerViewPagerEvent? = null

    init {
        setPageTransformer(true, VerticalPageTransformer())
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev ?: return false
        val intercepted =  super.onInterceptTouchEvent(swapXYOnMotionEvent(ev))
            swapXYOnMotionEvent(ev)
        return intercepted
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        timerViewPagerEventListener ?: throwInterfaceException()
        when(ev?.action){
            MotionEvent.ACTION_DOWN ->  timerViewPagerEventListener?.onViewPagerSwipeDown()
            MotionEvent.ACTION_UP ->    timerViewPagerEventListener?.onViewPagerSwipeUp()
        }
        return super.onTouchEvent(swapXYOnMotionEvent(ev ?: return false))
    }

    private fun throwInterfaceException():Boolean {
        throw RuntimeException("${this.javaClass.simpleName}: Parent Activity must implement TimerViewPagerEvent interface" )
    }

    private fun swapXYOnMotionEvent(motionEvent: MotionEvent): MotionEvent{
        with(motionEvent){
            val newX = (y/height)*width
            val newY = (x/width) * height
            setLocation(newX, newY)
        }
        return motionEvent
    }

    interface TimerViewPagerEvent{
        fun onViewPagerSwipeUp()
        fun onViewPagerSwipeDown()
    }
}

private class VerticalPageTransformer: ViewPager.PageTransformer{

    override fun transformPage(page: View, position: Float) {
        when {
            position <-1 -> makePageInvisible(page)
            position <=1 -> overrideXTranslationWithYTranslation(page, position)
            else -> makePageInvisible(page)
        }
    }

    //region 2nd layer functions
    private fun overrideXTranslationWithYTranslation(page: View, position: Float) {
        page.apply {
            alpha = 1f
            //Offset default X translation since we want Y
            translationX = width * -position

            val yPositionDestination = position * height
            translationY = yPositionDestination
        }
    }

    private fun makePageInvisible(page: View) {
        page.alpha = 0f
    }

    //endregion

}