package com.inviscidlabs.enkel.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.viewmodel.TimerViewModel
import kotlinx.android.synthetic.main.fragment_timer.*

private const val ARG_TIME = "args_timeInMilliseconds"


class TimerFragment: Fragment(){


    lateinit var appContext: Context
    private var timerTime = 0L
    private var fragmentInterface: OnTimerFragmentResult? = null
    private lateinit var wrapper:ContextThemeWrapper

    //Views


//region Lifecycle functions

    override fun onAttach(context: Context?) {
        if(context!=null) {
            appContext = context.applicationContext
            wrapper = ContextThemeWrapper(appContext, R.style.AppTheme)
        } else {
            throw RuntimeException(this::class.java.simpleName)
        }

        if(arguments?.containsKey(ARG_TIME)==null){
            throw RuntimeException(this::class.java.simpleName + " must be created " +
                    "using the newInstance constructor with a valid Long > 0")
        } else {
            timerTime = arguments!!.getLong(ARG_TIME)
        }

        if(context is OnTimerFragmentResult){
            fragmentInterface = context
        } else {
            throw RuntimeException("${context.toString()} must implement OnTimerFragmentResult")
        }
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(savedInstanceState==null) setInitialDrawableColors()

        val factory = TimerViewModel.Factory(timerTime)
        val viewModel = ViewModelProviders.of(this, factory)
                .get(TimerViewModel::class.java)
        observeTimeExpired((viewModel))
        observeViewModel(viewModel)
        setupPlayButton(viewModel)
        setupResetButton(viewModel)
        progressBar.max = timerTime.toInt()
    }

    override fun onDetach() {
        super.onDetach()
        fragmentInterface = null
    }
//endregion

//region Top Layer Functions

    private fun setupPlayButton(viewModel: TimerViewModel){
        button_playpause.setOnClickListener {
            val isPaused = viewModel.isPaused.value ?: true
            viewModel.setPauseStatus(!isPaused)
            with(fragmentInterface?: return@setOnClickListener){
                if(isPaused){
                    playClicked()
                } else {
                    pauseClicked()
                }
            }
        }
    }

    private fun setupResetButton(viewModel: TimerViewModel){
        button_reset.setOnClickListener {
            setResetDrawable()
            viewModel.resetTimer()
            fragmentInterface?.pauseClicked()
        }
    }

    private fun observeViewModel(viewModel: TimerViewModel) {
        observeTimeElapsed(viewModel)
        observePauseStatus(viewModel)
    }


//endregion

//region 2nd Layer Functions

    private fun observeTimeElapsed(viewModel: TimerViewModel){
        viewModel.timeElapsed.observe(this, Observer{timeElapsed->
            time_text.text = timeElapsed!!.toString()
            setProgress(timeElapsed)
        })
    }

    private fun observePauseStatus(viewModel: TimerViewModel){
        viewModel.isPaused.observe(this, Observer{ isPaused->
            if(isPaused!=null){
                adjustPlayPauseButton(isPaused)
            }
        })
    }

    private fun observeTimeExpired(viewModel: TimerViewModel){
        viewModel.timeIsExpired.observe(this, Observer {isExpired->
            if(isExpired!=null && isExpired){
                fragmentInterface?.timerDone(timerTime)
                viewModel.resetTimer()
                setResetDrawable()
            }
        })
    }

    private fun adjustPlayPauseButton(isPaused: Boolean){
        when(isPaused){
            true -> setPlayDrawable()
            else -> setPauseDrawable()
        }
    }
    private fun setProgress(timeElapsed: Long) {
        if (timerTime > 0 && timeElapsed <= timerTime) {
            progressBar.progress = timeElapsed.toInt() //((timeElapsed / timerTime*100).toInt())
        }
    }
    private fun setInitialDrawableColors(){
        setResetDrawable()
        setPlayDrawable()
    }
//endregion


//region Bottom Layer Functions

    private fun setPlayDrawable(){
        button_playpause.apply {
            setImageResource(R.drawable.anim_play_to_pause)
            drawable.startAsAnimatable()
        }
    }

    private fun setPauseDrawable(){
        button_playpause.apply {
            setImageResource(R.drawable.anim_pause_to_play)
            drawable.startAsAnimatable()
        }
    }

    private fun setResetDrawable(){
        button_reset.apply {
            setImageResource(R.drawable.anim_reset_twirl)
            drawable.startAsAnimatable()
        }
    }


//endregion

//Utilities
    private fun themedDrawable(drawableID: Int) = VectorDrawableCompat.create(resources, drawableID, wrapper.theme)

    private fun Drawable.startAsAnimatable() = (this as Animatable).start()


    interface OnTimerFragmentResult{
        fun timerDone(totalTime: Long)
        fun playClicked()
        fun pauseClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance(timeInSeconds: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_TIME, timeInSeconds)
            }
        }
    }

}
