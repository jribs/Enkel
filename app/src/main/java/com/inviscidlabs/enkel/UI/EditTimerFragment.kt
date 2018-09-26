package com.inviscidlabs.enkel.UI

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.viewmodel.EditTimerViewModel
import kotlinx.android.synthetic.main.fragment_edit_timer.*

private const val ARG_INSERT = "ARG_INSERT"

class EditTimerFragment : Fragment() {
    private var listener: OnEditTimerEvent? = null

    private val viewModel: EditTimerViewModel by lazy {
        ViewModelProviders.of(this).get(EditTimerViewModel::class.java)
    }

//region Lifecycle Methods

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnEditTimerEvent) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnEditTimerEvent")
        }

        setInsertMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_edit_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instantiateButtons()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
//endregion


//region 2nd Layer Functions
    private fun setInsertMode() {
        if (arguments?.containsKey(ARG_INSERT) == null || arguments == null) {
            throw RuntimeException(this::class.java.simpleName + " must be created " +
                    "using the newInstance constructor with a valid Boolean")
        } else {
            viewModel.setInsertMode(arguments!!.getBoolean(ARG_INSERT))
        }
    }

    private fun instantiateButtons() {
        button_dialog_save.setOnClickListener {
            if(isValidTimerInput()){
                viewModel.saveTimer(compileTimerFromCurrentUserInput())
            }
        }
        button_dialog_cancel.setOnClickListener {activity?.onBackPressed()}
    }

//endregion

//region Bottom Layer Functions

    private fun isValidTimerInput(): Boolean {
        with(field_edit_timer.text){
            return (!TextUtils.isEmpty(this)&& this.toString().toLongOrNull()!=null)
        }
    }

    private fun compileTimerFromCurrentUserInput(): Long {
        //EditText checked to be long in isValidTimerInput()
        return field_edit_timer.text.toString().toLong()
    }

//endregion

    interface OnEditTimerEvent {
        fun onTimerSave(timerToSave: TimerEntity)
    }

    companion object {
        @JvmStatic
        fun newInstance(insertNewTimerMode: Boolean) = EditTimerFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_INSERT, insertNewTimerMode)
            }
        }
    }

}




