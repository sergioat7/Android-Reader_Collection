/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/1/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat

fun Activity.getScreenSize(): Pair<Int, Int> {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun Context.getCustomColor(colorId: Int): Int = ResourcesCompat.getColor(resources, colorId, null)

fun Context.getCustomFont(fontId: Int): Typeface? = ResourcesCompat.getFont(this, fontId)

fun Context?.isDarkMode(): Boolean = this?.resources?.configuration?.uiMode?.and(
    Configuration.UI_MODE_NIGHT_MASK,
) == Configuration.UI_MODE_NIGHT_YES