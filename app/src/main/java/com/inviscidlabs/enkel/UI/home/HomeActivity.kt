package com.inviscidlabs.enkel.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.ui.custom.TimerSlidePagerAdapter
import com.inviscidlabs.enkel.ui.custom.VerticalTimerViewPager
import com.inviscidlabs.enkel.ui.edit_timer.ARG_TIMERID
import com.inviscidlabs.enkel.ui.edit_timer.EditTimerActivity
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_EDIT_CANCELED
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_TIMER_SAVED
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"
private const val REQ_LAUNCH_EDITTIMER = 461

class MainActivity : AppCompatActivity(), TimerFragment.OnTimerFragmentResult,
                    VerticalTimerViewPager.TimerViewPagerEvent{

//region Local Constants
    private val TAG = this.javaClass.simpleName
//endregion

    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)
    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java) }

//region Lifecycle Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(home_toolbar)

        savedInstanceState ?: createNotificationChannel()
        setupPagerAdapter()
        observeTimers()
        observeSelectedTimer()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_add_timer -> startEditActivity(true)
            R.id.menu_delete_timer -> deleteCurrentFragment()
            R.id.menu_edit_timer -> startEditActivity(false)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val intentPair = Pair(requestCode, resultCode)
        when(intentPair){
            Pair(REQ_LAUNCH_EDITTIMER, RESULT_EDIT_CANCELED) -> super.onActivityResult(requestCode, resultCode, data)
            Pair(REQ_LAUNCH_EDITTIMER, RESULT_TIMER_SAVED) -> respondToSavedTimer(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
//endregion

//region Implemented Functions
    override fun timerDone(totalTime: Long) {
        notifyTimerDone(totalTime)
    }

    override fun onViewPagerSwipeUp() {
       viewModel.onSwipeUp()
    }

    override fun onViewPagerSwipeDown() {
        viewModel.onSwipeDown()
    }
//endregion

//region 2nd Layer Functions
    private fun setupPagerAdapter() {
        val pagerAdapter = TimerSlidePagerAdapter(
                fragmentManager = supportFragmentManager,
                timers = viewModel?.timers?.value)

        with(pager){
            adapter = pagerAdapter
            timerViewPagerEventListener = this@MainActivity
        }
    }
    //TODO use SharedPreferences to select right timer
    //TODO make pagerstrip indicate the # of timers
    private fun observeTimers(){
        viewModel.timers.observe(this, Observer {timerList->
            timerList ?: Log.e(TAG, "List of timers is null")
                    .also {return@Observer}
            setupPagerAdapter()
        })
    }

    //TODO test
    private fun observeSelectedTimer() {
        viewModel.selectedTimerIndex.observe(this, Observer {selectedTimerIndex->
            selectedTimerIndex ?: return@Observer
            pager.currentItem = selectedTimerIndex
        })
    }

    private fun startEditActivity(insertNewTimer: Boolean){
        var selectedTimerID: Int? = null
        if(!insertNewTimer){
            selectedTimerID = viewModel.selectedTimerIndex.value
        }

        val startEditActivityIntent = Intent(this, EditTimerActivity::class.java).apply {
            putExtra(ARG_TIMERID, selectedTimerID ?: -1)
        }
        startActivityForResult(startEditActivityIntent, REQ_LAUNCH_EDITTIMER)
    }

    private fun respondToSavedTimer(returnedData: Intent?) {
        if(returnedData==null){
            Log.e(TAG, "No data returned from EditTimerActivity saved result")
            return
        }
        val returnedTimerID: Int = returnedData.extras.getInt(ARG_TIMERID)

        viewModel.timerSuccessfullySaved(savedTimerID = returnedTimerID)
    }


    private fun deleteCurrentFragment(){
        viewModel.deleteTimerClicked()
    }

//region 3rd layer functions
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