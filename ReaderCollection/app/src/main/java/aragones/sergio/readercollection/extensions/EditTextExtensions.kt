/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import com.google.android.material.datepicker.MaterialDatePicker
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

fun EditText.showOrHidePassword(imageButton: ImageButton) {

    if (this.transformationMethod is HideReturnsTransformationMethod) {

        imageButton.setImageResource(R.drawable.ic_show_password)
        this.transformationMethod = PasswordTransformationMethod.getInstance()
    } else {

        imageButton.setImageResource(R.drawable.ic_hide_password)
        this.transformationMethod = HideReturnsTransformationMethod.getInstance()
    }
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