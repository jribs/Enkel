package com.inviscidlabs.enkel

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.view.Menu
import com.inviscidlabs.enkel.UI.TimerFragment
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"

class MainActivity : AppCompatActivity() {

    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)

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


    //TODO use DI context
    private fun notifyTimerDone(){

            val mContext = applicationContext
            val mBuilder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            with(mBuilder){
                setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                setContentTitle("Enkel Timer Done")
                setContentText("10 seconds is up")
                setStyle(NotificationCompat.BigTextStyle().bigText("10 seconds is up"))
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
                setAutoCancel(true)
            }

            NotificationManagerCompat.from(mContext).notify(mNotificationID.incrementAndGet(), mBuilder.build())

    }

}
