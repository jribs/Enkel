package com.inviscidlabs.enkel.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.app.HomeActivityForegroundEvent
import com.inviscidlabs.enkel.app.RxEventBus
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.ui.home.custom.SelectedTimerChangedListener
import com.inviscidlabs.enkel.ui.home.custom.TimerSlidePagerAdapter
import com.inviscidlabs.enkel.ui.edit_timer.ARG_TIMERID
import com.inviscidlabs.enkel.ui.edit_timer.EditTimerActivity
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_EDIT_CANCELED
import com.inviscidlabs.enkel.ui.edit_timer.RESULT_TIMER_SAVED
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import com.inviscidlabs.enkel.viewmodel.service.EnkelTimerService
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "enkelTime"
private const val REQ_LAUNCH_EDITTIMER = 461

class MainActivity : AppCompatActivity(), ControlsFragment.ControlsFragmentEvent{


    //region Local Constants
    private val TAG = this.javaClass.simpleName
//endregion

    //TODO store in ViewModel or savedState
    private val mNotificationID = AtomicInteger(0)
    private lateinit var viewModel: HomeViewModel


//region Lifecycle Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, HomeViewModel.Factory(application))
                .get(HomeViewModel::class.java)

        setContentView(R.layout.activity_main)
        setSupportActionBar(home_toolbar)
        observeTimers()
    }

    override fun onStart() {
        super.onStart()
        RxEventBus.post(HomeActivityForegroundEvent(true))
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
        data?:return
        val intentPair = Pair(requestCode, resultCode)
        when(intentPair){
            Pair(REQ_LAUNCH_EDITTIMER, RESULT_EDIT_CANCELED) -> super.onActivityResult(requestCode, resultCode, data)
            Pair(REQ_LAUNCH_EDITTIMER, RESULT_TIMER_SAVED) -> respondToSavedTimer(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        RxEventBus.post(HomeActivityForegroundEvent(false))
        super.onStop()
    }

    override fun newTimerClicked() {
        startEditActivity(true)
    }
//endregion

//region 2nd Layer Functions

    private fun setupPagerAdapter() {
        val pagerAdapter = TimerSlidePagerAdapter(
                fragmentManager = supportFragmentManager,
                timers = viewModel.timers.value)
        with(pager){
            adapter = pagerAdapter
            addOnPageChangeListener(SelectedTimerChangedListener(viewModel))
            val flingDistance: Field = ViewPager::class.java.getDeclaredField("mFlingDistance")
            flingDistance.isAccessible = true
            flingDistance.set(this, 0)
        }
    }

    //TODO use SharedPreferences to select right timer
    //TODO make pagerstrip indicate the # of timers
    private fun observeTimers(){
        viewModel.timers.observe(this, Observer {timerList->
            timerList ?: Log.e(TAG, "List of timers is null")
                    .also {return@Observer}
            setupPagerAdapter()
            pager.setCurrentItem(viewModel.selectedTimerIndex, false)
        })
    }

    private fun startEditActivity(insertNewTimer: Boolean){
        var selectedTimerID: Int? = null
        if(!insertNewTimer){
            //TODO change to selectedTimerID selectedTimerID = viewModel.selectedTimerIndex.value
        }

        val startEditActivityIntent = Intent(this, EditTimerActivity::class.java).apply {
            putExtra(ARG_TIMERID, selectedTimerID ?: -1)
        }
        startActivityForResult(startEditActivityIntent, REQ_LAUNCH_EDITTIMER)
    }

    private fun respondToSavedTimer(returnedData: Intent) {
        val returnedTimerID: Int = returnedData.extras.getInt(ARG_TIMERID)
        viewModel.timerSuccessfullySaved(savedTimerID = returnedTimerID)
    }

    private fun deleteCurrentFragment(){
        viewModel.deleteTimerClicked()
    }

//endregion

}
