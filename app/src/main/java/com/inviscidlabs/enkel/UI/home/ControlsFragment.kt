package com.inviscidlabs.enkel.ui.home

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inviscidlabs.enkel.R
import kotlinx.android.synthetic.main.fragment_controls.*


class ControlsFragment : Fragment() {
    private var controlEventListener: OnControlEvent? = null

//region LifeCycle Functions
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnControlEvent) {
            controlEventListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnControlEvent")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setupButtonClickListeners()
        super.onActivityCreated(savedInstanceState)
    }


    override fun onDetach() {
        super.onDetach()
        controlEventListener = null
    }
//endregion

//region 2nd Layer Functions

    private fun setupButtonClickListeners() {
        with(controlEventListener ?: return) {
            fab_playpause.setOnClickListener {playpauseClicked()}
            button_delete_reset.setOnClickListener {deleteResetClicked()}
            button_add_timer.setOnClickListener { addButtonClicked() }
        }
    }


//endregion

//Utilities
    interface OnControlEvent {
        // TODO: Update argument type and name
        fun playpauseClicked()
        fun deleteResetClicked()
        fun addButtonClicked()
    }

}
