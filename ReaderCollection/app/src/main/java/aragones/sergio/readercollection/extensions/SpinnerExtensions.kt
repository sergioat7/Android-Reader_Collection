/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.res.ColorStateList
import android.widget.Spinner
import androidx.core.content.ContextCompat
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.SpinnerAdapter

fun Spinner.setup(
    values: List<String>,
    currentPosition: Int,
    firstOptionEnabled: Boolean = false,
    rounded: Boolean = false,
    title: String? = null,
) {
    backgroundTintList = ColorStateList.valueOf(
        ContextCompat.getColor(context, R.color.colorPrimary)
    )

    adapter = SpinnerAdapter(
        context,
        values,
        firstOptionEnabled,
        rounded,
        title
    ).apply { setDropDownViewResource(R.layout.item_spinner_dropdown) }
    setSelection(currentPosition)
}