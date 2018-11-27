package com.inviscidlabs.enkel.ui.home

import android.app.Application
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.viewmodel.ActiveTimerViewModel
import kotlinx.android.synthetic.main.fragment_timer.*

private const val ARG_FRAGTIMER_ID_TIMER = "args_timerID"
private const val ARG_FRAGTIMER_TIME = "args_timeInMilliseconds"


class TimerFragment: Fragment(){

    lateinit var appContext: Context
    lateinit var application: Application
    private var timerTime = 0L
    private var timerID: Int = -1
    private var fragmentInterface: OnTimerFragmentResult? = null
    private lateinit var wrapper:ContextThemeWrapper

//region Lifecycle functions
    override fun onAttach(context: Context?) {
        if(context!=null) {
            appContext = context.applicationContext
            wrapper = ContextThemeWrapper(appContext, R.style.AppTheme)
        } else {
            throw RuntimeException("${this::class.java.simpleName}: parent has no context")
        }

        instantiateFragmentEventInterface(context)
        instantiateAppObjectForViewModel()
        extractBundledArgumentsIfAvailable()
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val application = activity?.application ?: return
        val factory = ActiveTimerViewModel.Factory(timerID, timerTime, application)
        val viewModel = ViewModelProviders.of(this, factory)
                .get(ActiveTimerViewModel::class.java)

        observeTimeExpired((viewModel))
        observeTimeElapsed(viewModel)

        progressBar.max = timerTime.toInt()
    }

    override fun onDetach() {
        super.onDetach()
        fragmentInterface = null
    }
    //endregion

//region Top Layer Functions

    private fun extractBundledArgumentsIfAvailable() {
        val hasTimerID = arguments?.containsKey(ARG_FRAGTIMER_ID_TIMER)
        val hasTimeRemaining = arguments?.containsKey(ARG_FRAGTIMER_TIME)

        if (hasTimerID == null || hasTimeRemaining==null) {
            throwIncorrectArgumentsException()
            return
        } else if(hasTimeRemaining && hasTimerID) {
            timerTime = arguments!!.getLong(ARG_FRAGTIMER_TIME)
            timerID = arguments!!.getInt(ARG_FRAGTIMER_ID_TIMER)
        } else {
            throwIncorrectArgumentsException()
            return
        }
    }

//    private fun setupPlayButton(viewModel: ActiveTimerViewModel){
//        button_playpause.setOnClickListener {
//            val isPaused = viewModel.isPaused.value ?: true
//            viewModel.setPauseStatus(!isPaused)
//        }
//    }
//
//    private fun setupResetButton(viewModel: ActiveTimerViewModel){
//        button_reset.setOnClickListener {
//            setResetDrawable()
//            viewModel.resetTimer()
//        }
//    }

    private fun observeTimeElapsed(viewModel: ActiveTimerViewModel){
        viewModel.timeRemaining.observe(this, Observer{ timeElapsed->
            if(timeElapsed!=null) {
                time_text.text = DateUtils.formatElapsedTime(timeElapsed)
                setProgress(timeElapsed)
            }
        })
    }

    private fun observeTimeExpired(viewModel: ActiveTimerViewModel){
        viewModel.timeIsExpired.observe(this, Observer {isExpired->
            if(isExpired!=null && isExpired){
                fragmentInterface?.timerDone(timerTime)
                viewModel.resetTimer()
            }
        })
    }
//endregion

//region 2nd Layer Functions
    private fun instantiateFragmentEventInterface(context: Context) {
        if (context is OnTimerFragmentResult) {
            fragmentInterface = context
        } else {
            throw RuntimeException("${context.toString()} must implement OnTimerFragmentResult")
        }
    }

    private fun instantiateAppObjectForViewModel() {
        if (activity != null) {
            application = (activity as AppCompatActivity).application
        } else {
            throw java.lang.RuntimeException("${this.javaClass.simpleName} needs a parent " +
                    "activity to pass application to viewModel")
        }
    }

    private fun setProgress(timeElapsed: Long) {
        if (timerTime > 0 && timeElapsed <= timerTime) {
            progressBar.progress = timeElapsed.toInt() //((timeRemaining / timerTime*100).toInt())
        }
    }


//endregion


//Utilities
    private fun throwIncorrectArgumentsException() {
        throw RuntimeException(this@TimerFragment::class.java.simpleName + " must be created " +
                "using the newInstance constructor with a valid Long > 0")
    }

    interface OnTimerFragmentResult{
        fun timerDone(totalTime: Long)
    }

    companion object {
        @JvmStatic
        fun newInstance(timerID: Int, timeRemaining: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_FRAGTIMER_ID_TIMER, timerID)
                putLong(ARG_FRAGTIMER_TIME, timeRemaining)
            }
        }
    }

}
