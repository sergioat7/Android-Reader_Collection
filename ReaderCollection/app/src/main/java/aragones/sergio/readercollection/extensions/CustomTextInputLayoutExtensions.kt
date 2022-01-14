/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.CustomTextInputLayoutBinding
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.util.*

fun CustomTextInputLayoutBinding.setError(text: String?) {
    if (textInputLayout.error != text) {
        textInputLayout.error = text
        textInputLayout.errorIconDrawable = null
        textInputLayout.isErrorEnabled = text != null
    }
}

inline fun CustomTextInputLayoutBinding.doAfterTextChanged(
    crossinline action: (text: Editable?) -> Unit
): TextWatcher = textInputEditText.doAfterTextChanged(action)

fun CustomTextInputLayoutBinding.setOnClickListener(onClickListener: View.OnClickListener) {
    textInputEditText.setOnClickListener(onClickListener)
}

fun CustomTextInputLayoutBinding.setEndIconOnClickListener(endIconOnClickListener: View.OnClickListener) {
    textInputLayout.setEndIconOnClickListener(endIconOnClickListener)
}

fun CustomTextInputLayoutBinding.getValue(): String {
    return this.textInputEditText.text.toString().trimStart().trimEnd()
}

fun CustomTextInputLayoutBinding.isBlank(): Boolean {
    return this.getValue().isBlank() || this.textInputEditText.text.toString() == Constants.NO_VALUE
}

fun CustomTextInputLayoutBinding.setHintStyle(id: Int) {
    this.textInputLayout.doOnLayout {
        this.textInputLayout.setHintTextAppearance(id)
    }
}

fun CustomTextInputLayoutBinding.showDatePicker(activity: FragmentActivity) {

    this.textInputEditText.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            val datePicker = getPicker(this.textInputEditText, activity)
            datePicker.show(activity.supportFragmentManager, "")
        }
    }
    this.textInputEditText.setOnClickListener {
        val datePicker = getPicker(this.textInputEditText, this.textInputEditText.context)
        datePicker.show(activity.supportFragmentManager, "")
    }
}

//region Private functions
private fun getPicker(editText: TextInputEditText, context: Context): MaterialDatePicker<Long> {

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