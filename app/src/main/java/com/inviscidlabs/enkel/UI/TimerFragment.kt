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
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_timer, container, false)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactoryFromArguments())
                .get(TimerViewModel::class.java)

        setupPlayButton(viewModel)
        observeViewModel(viewModel)
    }

    override fun onDetach() {
        super.onDetach()

    }
//endregion

//region Top Layer Functions

    private fun viewModelFactoryFromArguments(): TimerViewModel.Factory{
        //the arguments must exist, otherwise, will throw exception onAttach
        val countdownTime = arguments!!.getLong(ARG_TIME)
        return TimerViewModel.Factory(countdownTime)
    }

    private fun setupPlayButton(viewModel: TimerViewModel){
        button_playpause.setOnClickListener {
            val isPaused = viewModel.isPaused.value ?: true
            viewModel.setPauseStatus(!isPaused)
        }
    }

    private fun observeViewModel(viewModel: TimerViewModel) {
        observeTimeElapsed(viewModel)
        observePauseStatus(viewModel)
    }
//endregion

//region 2nd Layer Functions

    private fun observeTimeElapsed(viewModel: TimerViewModel){
        viewModel.timeElapsed.observe(this, Observer{
            time_text.setText(it!!.toString())
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
