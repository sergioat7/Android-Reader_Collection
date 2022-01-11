/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.text.Editable
import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import aragones.sergio.readercollection.databinding.CustomTextInputLayoutBinding

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