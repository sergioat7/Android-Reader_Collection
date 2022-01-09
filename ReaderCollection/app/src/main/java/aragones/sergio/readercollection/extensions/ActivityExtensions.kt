/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager

fun Activity.getScreenSize(): Pair<Int, Int> {

    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun Activity.hideSoftKeyboard() {
    currentFocus?.let { currentFocus ->

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    } ?: return
}