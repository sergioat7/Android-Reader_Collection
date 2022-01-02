/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.extensions

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import java.util.*

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {

    this.addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.onFocusChange(onFocusChange: () -> Unit) {

    this.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            onFocusChange()
        }
    }
}

fun EditText.clearErrors() {
    this.error = null
}

fun EditText.setReadOnly(value: Boolean, inputType: Int, lineColor: Int) {

    isFocusable = !value
    isFocusableInTouchMode = !value
    isEnabled = !value
    this.setRawInputType(inputType)
    this.backgroundTintList =
        if (value) ColorStateList.valueOf(Color.TRANSPARENT) else ColorStateList.valueOf(lineColor)
}

fun EditText.showDatePicker(context: Context) {

    this.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            val picker = getPicker(this, context)
            picker.show()
        }
    }
    this.setOnClickListener {
        val picker = getPicker(this, context)
        picker.show()
    }
}

fun EditText.getValue(): String {
    return this.text.toString().trimStart().trimEnd()
}

fun EditText.showOrHidePassword(imageButton: ImageButton, isDarkMode: Boolean) {

    if (this.transformationMethod is HideReturnsTransformationMethod) {

        val image =
            if (isDarkMode) R.drawable.ic_show_password_dark else R.drawable.ic_show_password_light
        imageButton.setImageResource(image)
        this.transformationMethod = PasswordTransformationMethod.getInstance()
    } else {

        val image =
            if (isDarkMode) R.drawable.ic_hide_password_dark else R.drawable.ic_hide_password_light
        imageButton.setImageResource(image)
        this.transformationMethod = HideReturnsTransformationMethod.getInstance()
    }
}

// MARK - Private functions

private fun getPicker(editText: EditText, context: Context): DatePickerDialog {

    val calendar = Calendar.getInstance()
    val currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth: Int = calendar.get(Calendar.MONTH)
    val currentYear: Int = calendar.get(Calendar.YEAR)
    return DatePickerDialog(context, { _, year, month, day ->

        val newDay = if (day < 10) "0${day}" else day.toString()
        val newMonth = if (month < 9) "0${month + 1}" else (month + 1).toString()
        val newDate = "${year}-${newMonth}-${newDay}"

        val language = SharedPreferencesHandler.getLanguage()

        val date = newDate.toDate(Constants.DATE_FORMAT, language)
        val dateString = date.toString(SharedPreferencesHandler.getDateFormatToShow(), language)

        editText.setText(dateString)
    }, currentYear, currentMonth, currentDay)
}