package com.inviscidlabs.enkel.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_controls.*
import java.lang.RuntimeException

class ControlsFragment(): Fragment() {

    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: HomeViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if(context!=null || context is FragmentActivity){
            viewModel = ViewModelProviders
                    .of((context as FragmentActivity), HomeViewModel.Factory(context.application))
                    .get(HomeViewModel::class.java)
        } else {
            throw RuntimeException("$TAG: Parent of this fragment is not FragmentActivity. Cannot instantiate required variables")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupResetButton()
        setupAddButton()
        setupPlayPause()
        observeInitialTimerState()
        observeTimerStatus()
    }

//region 2nd layer Functions
    private fun setupResetButton(){
        button_reset.setOnClickListener {
            viewModel.resetClicked()
            animateResetButton()
        }
    }

    private fun setupAddButton(){
        button_add_timer.setOnClickListener {
            viewModel.addTimer()
        }
    }

    private fun setupPlayPause(){
        fab_play_pause.setOnClickListener {
            viewModel.playPauseClicked()
        }
    }

    private fun observeTimerStatus() {
        viewModel.currentTimerIsPaused.observe(this, Observer {isPaused ->
            setFabState(isPaused ?: return@Observer, true)
        })
    }

    private fun observeInitialTimerState(){
        viewModel.initialTimerStatus.observe(this, Observer {isPaused ->
            setFabState(isPaused ?: return@Observer, false)
        })
    }
//endregion

//region UI
    private fun setFabState(timerIsPaused: Boolean, animateButtons: Boolean){
        fab_play_pause.apply {
            when (timerIsPaused) {
                true -> setImageResource(R.drawable.anim_play_to_pause)
                false ->setImageResource(R.drawable.anim_pause_to_play)
            }
        }
        if(animateButtons) (fab_play_pause.drawable as Animatable).start()
    }

    private fun animateResetButton() {
        (button_reset.compoundDrawables[0] as Animatable).start()
    }

//endregion

}