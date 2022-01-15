/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2022
 */

package aragones.sergio.readercollection.extensions

import androidx.core.view.doOnLayout
import aragones.sergio.readercollection.adapters.MenuAdapter
import aragones.sergio.readercollection.databinding.CustomDropdownTextInputLayoutBinding

fun CustomDropdownTextInputLayoutBinding.setHintStyle(id: Int) {
    textInputLayout.doOnLayout {
        textInputLayout.setHintTextAppearance(id)
    }
}

fun CustomDropdownTextInputLayoutBinding.getPosition(): Int {
    return (materialAutoCompleteTextView.adapter as MenuAdapter).values.indexOf(
        materialAutoCompleteTextView.text.toString()
    )
}

fun CustomDropdownTextInputLayoutBinding.getValue(): String {
    return materialAutoCompleteTextView.text.toString().trimStart().trimEnd()
}

fun CustomDropdownTextInputLayoutBinding.setValue(
    keys: List<String>,
    values: List<String>,
    currentKey: String?
) {

    if (materialAutoCompleteTextView.adapter == null) {
        materialAutoCompleteTextView.setAdapter(MenuAdapter(root.context, values))
    }

    currentKey?.let { key ->
        values.getOrNull(keys.indexOf(key))?.let { value ->
            materialAutoCompleteTextView.setText(value, false)
        }
    }
}