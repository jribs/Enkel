package com.inviscidlabs.enkel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.view.Menu
import com.inviscidlabs.enkel.UI.TimerFragment
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"

class MainActivity : AppCompatActivity(), TimerFragment.OnTimerFragmentResult {


    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        makeTimerFragment(10)
    }


    //TODO - Add menu
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun timerDone(totalTime: Long) {
        notifyTimerDone(totalTime)
    }

    private fun makeTimerFragment(countdownTime: Long){
        val fragment = TimerFragment.newInstance(countdownTime)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }


    //TODO use DI context
    private fun notifyTimerDone(totalTime: Long){

            val mContext = applicationContext
            val mBuilder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            with(mBuilder){
                setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                setContentTitle("Enkel Timer Done")
                setContentText("${totalTime} seconds is up")
                setStyle(NotificationCompat.BigTextStyle().bigText("${totalTime} seconds is up"))
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
            }

            NotificationManagerCompat.from(mContext).notify(mNotificationID.incrementAndGet(), mBuilder.build())

    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val name = "Enkel"
            val description = "Timer"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = description

            val notificationManager: NotificationManager =
                    getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}
