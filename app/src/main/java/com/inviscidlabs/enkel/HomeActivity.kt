package com.inviscidlabs.enkel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.view.Menu
import android.view.MenuItem
import com.inviscidlabs.enkel.UI.AddTimerDialog
import com.inviscidlabs.enkel.UI.TimerFragment
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"

class MainActivity : AppCompatActivity(), TimerFragment.OnTimerFragmentResult,
                    AddTimerDialog.OnAddTimerEvent{



    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)
    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java) }


//region Lifecycle Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        makeTimerFragment(10)
    }



    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_add_timer -> startAddTimerFragment()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

//endregion

//region Implemented Functions
    override fun timerDone(totalTime: Long) {
        notifyTimerDone(totalTime)
    }

    override fun onTimerSave(timerToSave: TimerEntity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


//endregion

//region 2nd Layer Functions

    //TODO consolidate fragment swappping to a function
    private fun makeTimerFragment(countdownTime: Long){
        val fragment = TimerFragment.newInstance(countdownTime)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun startAddTimerFragment(){
        val fragment: AddTimerDialog = AddTimerDialog()
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }

    //TODO use DI context
    private fun notifyTimerDone(totalTime: Long){

            val mContext = applicationContext
            val mBuilder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            with(mBuilder){
                setSmallIcon(R.drawable.play)
                setContentTitle("Enkel TimerEntity Done")
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
            val description = "TimerEntity"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = description

            val notificationManager: NotificationManager =
                    getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
//endregion


}
