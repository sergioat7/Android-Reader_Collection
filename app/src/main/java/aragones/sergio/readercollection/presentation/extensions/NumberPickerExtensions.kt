/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import android.widget.LinearLayout
import android.widget.NumberPicker


fun NumberPicker.setup(values: Array<String>) {

    this.minValue = 0
    this.maxValue = values.size - 1
    this.wrapSelectorWheel = true
    this.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    this.displayedValues = values
}

fun getPickerParams(): LinearLayout.LayoutParams {

    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.weight = 1f
    return params
}