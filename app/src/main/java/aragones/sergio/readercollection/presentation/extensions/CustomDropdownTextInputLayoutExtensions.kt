/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import androidx.core.view.doOnLayout
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.CustomDropdownTextInputLayoutBinding
import aragones.sergio.readercollection.presentation.ui.adapters.MenuAdapter
import aragones.sergio.readercollection.utils.Constants
import com.aragones.sergio.util.CustomDropdownType

fun CustomDropdownTextInputLayoutBinding.setHintStyle(id: Int) {
    textInputLayout.doOnLayout {
        textInputLayout.setHintTextAppearance(id)
    }
}

fun CustomDropdownTextInputLayoutBinding.getPosition(): Int =
    (materialAutoCompleteTextView.adapter as MenuAdapter).values.indexOf(
        materialAutoCompleteTextView.text.toString(),
    )

fun CustomDropdownTextInputLayoutBinding.getValue(): String = materialAutoCompleteTextView.text
    .toString()
    .trimStart()
    .trimEnd()

fun CustomDropdownTextInputLayoutBinding.setValue(currentKey: String?, type: CustomDropdownType) {
    val values = when (type) {
        CustomDropdownType.FORMAT -> {
            Constants.FORMATS.map { it.name }
        }
        CustomDropdownType.STATE -> {
            Constants.STATES.map { it.name }
        }
        CustomDropdownType.SORT_PARAM -> {
            root.context.resources
                .getStringArray(R.array.sorting_param_values)
                .toList()
        }
        CustomDropdownType.SORT_ORDER -> {
            root.context.resources
                .getStringArray(R.array.sorting_order_values)
                .toList()
        }
        CustomDropdownType.APP_THEME -> {
            root.context.resources
                .getStringArray(R.array.app_theme_values)
                .toList()
        }
    }

    if (materialAutoCompleteTextView.adapter == null) {
        materialAutoCompleteTextView.setAdapter(MenuAdapter(root.context, values))
    }

    currentKey?.let { key ->
        val keys = when (type) {
            CustomDropdownType.FORMAT -> {
                Constants.FORMATS.map { it.id }
            }
            CustomDropdownType.STATE -> {
                Constants.STATES.map { it.id }
            }
            CustomDropdownType.SORT_PARAM -> {
                root.context.resources
                    .getStringArray(R.array.sorting_param_keys)
                    .toList()
            }
            CustomDropdownType.SORT_ORDER -> {
                root.context.resources
                    .getStringArray(R.array.sorting_order_keys)
                    .toList()
            }
            CustomDropdownType.APP_THEME -> {
                root.context.resources
                    .getStringArray(R.array.app_theme_values)
                    .toList()
            }
        }
        values.getOrNull(keys.indexOf(key))?.let { value ->
            materialAutoCompleteTextView.setText(value, false)
        }
    }
}