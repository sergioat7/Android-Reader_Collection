/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.app.Activity
import android.view.View
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