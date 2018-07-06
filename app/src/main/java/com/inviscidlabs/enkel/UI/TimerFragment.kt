package com.inviscidlabs.enkel.UI

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.ViewModel.TimerViewModel
import kotlinx.android.synthetic.main.fragment_timer.*

private const val ARG_TIME = "args_timeInMilliseconds"

class TimerFragment: Fragment(){

    private var timerTime = 0L


//region Lifecycle functions

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(arguments?.containsKey(ARG_TIME)==null){
            throw RuntimeException(this::class.java.simpleName + " must be created " +
                    "using the newInstance constructor with a valid Long > 0")
        } else {
            timerTime = arguments!!.getLong(ARG_TIME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_timer, container, false)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = TimerViewModel.Factory(timerTime)
        val viewModel = ViewModelProviders.of(this, factory)
                .get(TimerViewModel::class.java)

        observeViewModel(viewModel)
        setupPlayButton(viewModel)
        setupResetButton(viewModel)
        //TODO if savedInstanceState, align with ViewModel
    }

    override fun onDetach() {
        super.onDetach()

    }
//endregion

//region Top Layer Functions

    private fun setupPlayButton(viewModel: TimerViewModel){
        button_playpause.setOnClickListener {
            val isPaused = viewModel.isPaused.value ?: true
            viewModel.setPauseStatus(!isPaused)
        }
    }

    private fun setupResetButton(viewModel: TimerViewModel){
        button_reset.setOnClickListener {
            viewModel.resetTimer()
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
            time_text.setText(timeElapsed!!.toString())
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

    private fun adjustPlayPauseButton(isPaused: Boolean){
        when(isPaused){
            true -> button_playpause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            else -> button_playpause.setImageResource(R.drawable.ic_pause_black_24dp)
        }

    }

    private fun setProgress(timeElapsed: Long) {
        if (timerTime > 0 && timeElapsed <= timerTime) {
            progressBar.progress = ((timeElapsed / timerTime*100).toInt())
            progressBar.max = timerTime.toInt()
            progressBar.progress = ((timeElapsed / timerTime*100).toInt())

        }
    }
//endregion

        companion object {
        @JvmStatic
        fun newInstance(timeInSeconds: Long) = TimerFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_TIME, timeInSeconds)
            }
        }
    }

}
