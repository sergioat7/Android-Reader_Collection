/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

fun EditText.setReadOnly(value: Boolean, inputType: Int, lineColor: Int) {

    isFocusable = !value
    isFocusableInTouchMode = !value
    isEnabled = !value
    this.setRawInputType(inputType)
    this.backgroundTintList =
        if (value) ColorStateList.valueOf(Color.TRANSPARENT) else ColorStateList.valueOf(lineColor)
}

fun EditText.showDatePicker(activity: FragmentActivity) {

    this.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            val datePicker = getPicker(this, activity)
            datePicker.show(activity.supportFragmentManager, "")
        }
    }
    this.setOnClickListener {
        val datePicker = getPicker(this, context)
        datePicker.show(activity.supportFragmentManager, "")
    }
}

fun EditText.getValue(): String {
    return this.text.toString().trimStart().trimEnd()
}

//region Private functions
private fun getPicker(editText: EditText, context: Context): MaterialDatePicker<Long> {

    return MaterialDatePicker.Builder
        .datePicker()
        .setTitleText(context.resources.getString(R.string.select_a_date))
        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
        .build().apply {
            addOnPositiveButtonClickListener {

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it

                val dateString = calendar.time.toString(
                    SharedPreferencesHandler.getDateFormatToShow(),
                    SharedPreferencesHandler.getLanguage()
                )

                editText.setText(dateString)
            }
        }
}
//endregion