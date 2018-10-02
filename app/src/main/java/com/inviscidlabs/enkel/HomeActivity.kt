package com.inviscidlabs.enkel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.inviscidlabs.enkel.ui.EditTimerFragment
import com.inviscidlabs.enkel.ui.TimerFragment
import com.inviscidlabs.enkel.ui.custom.TimerSlidePagerAdapter
import com.inviscidlabs.enkel.ui.custom.VerticalTimerViewPager
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"

class MainActivity : FragmentActivity(), TimerFragment.OnTimerFragmentResult,
                    EditTimerFragment.OnEditTimerEvent,
                    VerticalTimerViewPager.TimerViewPagerEvent{

    //TODO add TAG and use for error logging

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
        menuInflater.inflate(R.menu.home_activity, menu)
        return super.onPrepareOptionsMenu(menu)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menu?.clear()
//        menuInflater.inflate(R.menu.home_activity, menu)
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_add_timer -> startAddTimerFragment()
            R.id.menu_delete_timer -> deleteCurrentFragment()
            R.id.menu_edit_timer -> editCurrentFragment()
        }
        return super.onOptionsItemSelected(item)
    }

//endregion

//region Implemented Functions
    override fun timerDone(totalTime: Long) {
        notifyTimerDone(totalTime)
    }

    override fun onTimerSave(savedTimerID: Long) {
        //TODO loadNewTimer
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
            timerList ?: Log.e(this.javaClass.simpleName, "List of timers is null")
                    .also {return@Observer}
            setupPagerAdapter()
        })
    }


    private fun startEditTimerFragForEditMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun startAddTimerFragment(){
        val selectedTimerID = viewModel.selectedTimerIndex.value
        val fragment: EditTimerFragment = EditTimerFragment.newInstance(selectedTimerID)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
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
