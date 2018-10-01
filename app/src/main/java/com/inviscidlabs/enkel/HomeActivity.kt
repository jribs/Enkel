package com.inviscidlabs.enkel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.inviscidlabs.enkel.ui.EditTimerFragment
import com.inviscidlabs.enkel.ui.TimerFragment
import com.inviscidlabs.enkel.ui.custom.TimerSlidePagerAdapter
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"
private const val TAG_EDIT_TIMER = "editTimer"
private const val TAG_TIMER_FRAG = "timerFrag"

class MainActivity : AppCompatActivity(), TimerFragment.OnTimerFragmentResult,
                    EditTimerFragment.OnEditTimerEvent{



    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)
    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java) }


//region Lifecycle Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        savedInstanceState ?: createNotificationChannel()

        setupPagerAdapter()
        observeTimers()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_add_timer -> startAddTimerFragment()
            R.id.menu_delete_timer -> deleteCurrentFragment()
            R.id.menu_edit_timer -> editCurrentFragment()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

//endregion

//region Implemented Functions
    override fun timerDone(totalTime: Long) {
        notifyTimerDone(totalTime)
    }

    override fun onTimerSave(savedTimerID: Long) {
        //TODO loadNewTimer
    }

//endregion

//region 2nd Layer Functions
    private fun setupPagerAdapter() {
        val pagerAdapter = TimerSlidePagerAdapter(viewModel = viewModel, fragmentManager = supportFragmentManager)
        with(pager){
            adapter = pagerAdapter
            homeViewModel = viewModel
        }
    }

    private fun observeTimers(){
        viewModel.timers.observe(this, Observer {timerList->
            timerList ?: Log.e(this.javaClass.simpleName, "List of timers is null")
                    .also {return@Observer}

            pager.adapter?.notifyDataSetChanged()
            //TODO use SharedPreferences to select right timer
            //TODO make pagerstrip indicate the # of timers
        })
    }


    private fun startEditTimerFragForEditMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    private fun startAddTimerFragment(){
        val fragment: EditTimerFragment = EditTimerFragment.newInstance(true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, TAG_EDIT_TIMER)
                .commit()

    }

    private fun deleteCurrentFragment(){
        viewModel.deleteTimerClicked()
    }

    private fun editCurrentFragment(){

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
