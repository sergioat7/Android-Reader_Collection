/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import aragones.sergio.readercollection.R

fun Activity.handleStatusBar(view: View, hideActionBar: Boolean, isDarkMode: Boolean) {

    window.statusBarColor =
        ContextCompat.getColor(
            this,
            if (hideActionBar) R.color.colorSecondary else R.color.colorPrimary
        )
    WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !isDarkMode
    (this as? AppCompatActivity)?.supportActionBar?.apply {
        if (hideActionBar) hide() else show()
    }
}

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

fun Activity?.isDarkMode(): Boolean {
    return this?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}