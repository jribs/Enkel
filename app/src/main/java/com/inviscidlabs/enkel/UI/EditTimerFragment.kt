package com.inviscidlabs.enkel.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.viewmodel.EditTimerViewModel
import com.inviscidlabs.enkel.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.fragment_edit_timer.*

private const val ARG_TIMERID_TO_LOAD = "ARG_TIMER_ID"

class EditTimerFragment : Fragment() {

    private val TAG = this.javaClass.simpleName

    private var fragmentUIEventListener: OnEditTimerEvent? = null
    private var activityViewModel: HomeViewModel? = null
    private val fragmentViewModel: EditTimerViewModel by lazy {
        ViewModelProviders.of(this).get(EditTimerViewModel::class.java)
    }

//region Lifecycle Methods

    override fun onAttach(context: Context) {
        super.onAttach(context)
        makeSureActivityHasListener(context)
        communicateArgumentsToFragmentViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_edit_timer, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instantiateActivityViewModel()
        observeSavedTimerID()
        instantiateButtons()
    }

    override fun onDetach() {
        super.onDetach()
        fragmentUIEventListener = null
    }

//region 2nd Layer Functions

    private fun makeSureActivityHasListener(context: Context) {
        if (context is OnEditTimerEvent) {
            fragmentUIEventListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnEditTimerEvent")
        }
    }

    private fun communicateArgumentsToFragmentViewModel() {
        val timerIDArgument = arguments?.getInt(ARG_TIMERID_TO_LOAD)
        val insertModeFromArguments = (timerIDArgument==null)
        fragmentViewModel.setInsertMode(insertModeFromArguments)
        if(timerIDArgument != null){
            fragmentViewModel.loadTimer(timerIDArgument)
        }
    }

    private fun instantiateActivityViewModel() {
        when{
            (activity==null)                -> throwNoParentActivityException()
            (activity !is FragmentActivity) -> throwNotFragmentActivityException()

            else -> activityViewModel = ViewModelProviders.of(activity as FragmentActivity)
                                            .get(HomeViewModel::class.java)
        }
    }

    private fun instantiateButtons() {
        button_dialog_save.setOnClickListener {
            if(isValidTimerInput()){
                fragmentViewModel.saveTimer(compileTimerFromCurrentUserInput())
            }
        }
        button_dialog_cancel.setOnClickListener {activity?.onBackPressed()}
    }

    private fun observeSavedTimerID(){
        fragmentViewModel.timerSavedID.observe(this, Observer { timerID->
            timerID ?: return@Observer
            fragmentUIEventListener?.onTimerSave(timerID)
        })
    }

//endregion

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

    private fun throwNoParentActivityException() {
        throw RuntimeException("$TAG: Parent Activity context does not have the appropriate ViewModel attached to it." +
                 " Unable to load or save TimerEntity")
    }

    private fun throwNotFragmentActivityException() {
       throw RuntimeException("$TAG: Parent Activity must be a FragmentActivity")
    }
//endregion

    interface OnEditTimerEvent {
        fun onTimerSave(savedTimerID: Long)
    }

    companion object {
        @JvmStatic
        fun newInstance(timerToLoad: Int?) = EditTimerFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TIMERID_TO_LOAD, timerToLoad ?: return@apply)
            }
        }
    }

}




