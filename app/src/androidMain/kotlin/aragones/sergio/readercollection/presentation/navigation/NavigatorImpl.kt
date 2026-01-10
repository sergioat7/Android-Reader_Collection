/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2026
 */

package aragones.sergio.readercollection.presentation.navigation

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.landing.LandingActivity

class NavigatorImpl(private val context: Context) : Navigator {
    override fun goToLanding() {
        val intent = Intent(context, LandingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SKIP_ANIMATION", true)
        }
        val options = ActivityOptions
            .makeCustomAnimation(
                context,
                R.anim.slide_in_left,
                R.anim.slide_out_right,
            ).toBundle()
        context.startActivity(intent, options)
    }

    override fun goToMain(withOptions: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val options = ActivityOptions
            .makeCustomAnimation(
                context,
                R.anim.slide_in_right,
                R.anim.slide_out_left,
            ).toBundle()
            .takeIf { withOptions }
        context.startActivity(intent, options)
    }
}