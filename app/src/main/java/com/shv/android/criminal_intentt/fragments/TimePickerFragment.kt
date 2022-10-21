package com.shv.android.criminal_intentt.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.Calendar
import java.util.Date

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {
    interface Callbacks {
        fun onTimeSelected(h: Int, m: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeListener =
            TimePickerDialog.OnTimeSetListener { _, hour: Int, minute: Int ->
                val bundle = bundleOf(
                    ("hour" to hour),
                    ("minute" to minute)
                )
                setFragmentResult(RESULT_TIME_KEY, bundle)
            }

        val date = if (Build.VERSION.SDK_INT >= 33)
            arguments?.getSerializable(ARG_TIME, Date::class.java) as Date
        else
            arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            activity,
            timeListener,
            hour,
            minute,
            DateFormat.is24HourFormat(activity)
        )
    }




    companion object {
        fun newInstance(date: Date): TimePickerFragment {
            val arg = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = arg
            }
        }
    }
}