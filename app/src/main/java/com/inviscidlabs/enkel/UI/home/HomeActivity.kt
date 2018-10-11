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
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.ui.custom.SelectedTimerChangedListener
import com.inviscidlabs.enkel.ui.custom.TimerSlidePagerAdapter
import com.inviscidlabs.enkel.ui.edit_timer.ARG_TIMERID
import com.inviscidlabs.enkel.ui.edit_timer.EditTimerActivity
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_EDIT_CANCELED
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_TIMER_SAVED
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import com.inviscidlabs.enkel.viewmodel.service.ACTION_PAUSE
import com.inviscidlabs.enkel.viewmodel.service.ACTION_START_TIMER
import com.inviscidlabs.enkel.viewmodel.service.EnkelTimerService
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"
private const val REQ_LAUNCH_EDITTIMER = 461

class MainActivity : AppCompatActivity(), TimerFragment.OnTimerFragmentResult{

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
        setupPagerAdapter()

        savedInstanceState ?: createNotificationChannel()

        observeTimers()
        observeTargetedTimerSelection()
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

    //TODO, make current Timer be a Timer Object easier implementation
    override fun playClicked() {
        val currentTimerIndex = viewModel.selectedTimerIndex.value ?: 0
        val currentTimer = viewModel.timers.value?.get(currentTimerIndex) ?: return
        val playServiceIntent = makeServiceIntentWithExtras(currentTimer)
        playServiceIntent.action = ACTION_START_TIMER
        startService(playServiceIntent)
    }

    override fun pauseClicked() {
        val currentTimerIndex = viewModel.selectedTimerIndex.value ?: 0
        val currentTimer = viewModel.timers.value?.get(currentTimerIndex) ?: return
        val pauseServiceIntent = makeServiceIntentWithExtras(currentTimer)
        pauseServiceIntent.action = ACTION_PAUSE
        startService(pauseServiceIntent)
    }

    //endregion

//region 2nd Layer Functions
    private fun setupPagerAdapter() {
        val pagerAdapter = TimerSlidePagerAdapter(
                fragmentManager = supportFragmentManager,
                timers = viewModel?.timers?.value)
        with(pager){
            adapter = pagerAdapter
            addOnPageChangeListener(SelectedTimerChangedListener(viewModel))
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
    private fun observeTargetedTimerSelection(){
        viewModel.targetedTimerIndexSelection.observe(this, Observer {index ->
            index ?: return@Observer
            when(index){
                -1 -> return@Observer
                else -> pager.currentItem = index
            }
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
        if(returnedData==null || returnedData.extras.getInt(ARG_TIMERID)==null){
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


//region Utilities
    private fun makeServiceIntentWithExtras(timer: TimerEntity): Intent{
        val intent = Intent(this, EnkelTimerService::class.java).apply {
            putExtra(getString(R.string.key_timer_id), timer.timerID)
            putExtra(getString(R.string.key_timer_time), timer.timeInMS)
        }
        return intent
    }




}
