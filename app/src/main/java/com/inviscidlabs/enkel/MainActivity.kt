package com.inviscidlabs.enkel

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.inviscidlabs.enkel.UI.TimerFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeTimerFragment(120)
    }


    //TODO - Add menu
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    private fun makeTimerFragment(countdownTime: Long){
        val fragment = TimerFragment.newInstance(countdownTime)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }


}
