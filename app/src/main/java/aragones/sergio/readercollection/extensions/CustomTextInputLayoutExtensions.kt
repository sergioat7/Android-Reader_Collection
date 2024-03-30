/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.SharedPreferencesHandler
import aragones.sergio.readercollection.databinding.CustomTextInputLayoutBinding
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.TimeZone

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

fun CustomTextInputLayoutBinding.getSpannableFor(style: Int): SpannableStringBuilder {

    val text = textInputEditText.text.toString()
    val spannable = SpannableStringBuilder(text)
    val regex = Regex("\\*(.*?)\\*")

    val asterisksIndices = mutableListOf<Int>()
    val matches = regex.findAll(text)
    for (match in matches) {

        val initPosition = match.range.first + 1
        val endPosition = initPosition + match.groupValues[1].length
        spannable.setSpan(
            StyleSpan(style),
            initPosition,
            endPosition,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        asterisksIndices.add(initPosition - 1)
        asterisksIndices.add(endPosition)
    }

    for (index in asterisksIndices) {
        spannable.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    textInputEditText.context,
                    R.color.textSecondaryThin
                )
            ),
            index,
            index + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
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

    val currentDateInMillis = editText.text.toString().toDate(
        SharedPreferencesHandler.dateFormatToShow,
        SharedPreferencesHandler.language,
        TimeZone.getTimeZone("UTC")
    )?.time ?: MaterialDatePicker.todayInUtcMilliseconds()

    return MaterialDatePicker.Builder
        .datePicker()
        .setTitleText(context.resources.getString(R.string.select_a_date))
        .setSelection(currentDateInMillis)
        .build().apply {
            addOnPositiveButtonClickListener {

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it

                val dateString = calendar.time.toString(
                    SharedPreferencesHandler.dateFormatToShow,
                    SharedPreferencesHandler.language
                )

                editText.setText(dateString)
            }
        }
}
//endregion