package com.inviscidlabs.enkel.ui.edit_timer

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.inviscidlabs.enkel.R
import com.inviscidlabs.enkel.model.entity.TimerEntity
import com.inviscidlabs.enkel.viewmodel.EditTimerViewModel
import kotlinx.android.synthetic.main.activity_edit_timer.*

const val ARG_TIMERID = "passed_timerID"
const val RESULT_TIMER_SAVED = 69
const val RESULT_EDIT_CANCELED = 99

class EditTimerActivity: AppCompatActivity(){

    private val viewModel: EditTimerViewModel by lazy {
        ViewModelProviders.of(this).get(EditTimerViewModel::class.java)
    }

//region Lifecycle Functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_timer)

        communicateArgumentsToViewModel()

        observeSavedTimerID()
        observeLoadedTimer()
        listenForCancelClick()
        listenForSaveClick()
    }
//endregion

//region 2nd Layer Functions
    private fun communicateArgumentsToViewModel() {
        val timerIDArgument = intent.extras.getInt(ARG_TIMERID)
        viewModel.communicateTimerID(timerIDArgument)
    }

    private fun listenForSaveClick() {
        button_dialog_save.setOnClickListener {
            if (isValidTimerInput()) {
                viewModel.saveTimerClicked(compileTimerFromCurrentUserInput())
            }
        }
    }

    private fun listenForCancelClick() {
        button_dialog_cancel.setOnClickListener {
            setResult(RESULT_EDIT_CANCELED)
            finish()
        }
    }

    private fun observeSavedTimerID(){
        viewModel.timerSavedID.observe(this, Observer { timerID->
            timerID ?: return@Observer
            val returnData = Intent().apply {
                putExtra(ARG_TIMERID, timerID)
            }

            setResult(RESULT_TIMER_SAVED, returnData)
            finish()
        })
    }

    private fun observeLoadedTimer() {
        viewModel.loadedTimer.observe(this, Observer {timer->
            timer ?: return@Observer
            populateFieldsWithTimer(timer)
        })
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

    private fun populateFieldsWithTimer(timer: TimerEntity) {
        field_edit_timer.setText(timer.timeInMS.toString())
    }

//endregion

}