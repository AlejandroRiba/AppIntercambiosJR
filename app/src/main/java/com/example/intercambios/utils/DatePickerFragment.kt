package com.example.intercambios.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.intercambios.R
import java.util.Calendar

class DatePickerFragment(val listener: (day: Int, month: Int, year: Int) -> Unit) :
    DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        listener(day, month + 1, year)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Crear el DatePickerDialog
        val datePickerDialog = DatePickerDialog(activity as Context, R.style.PickerTheme, this, year, month, day)

        // Restringir las fechas anteriores a la fecha actual
        datePickerDialog.datePicker.minDate = c.timeInMillis

        return datePickerDialog
    }

}
