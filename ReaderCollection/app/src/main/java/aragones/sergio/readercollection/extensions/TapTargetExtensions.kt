/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/2/2022
 */

package aragones.sergio.readercollection.extensions

import android.app.Activity
import aragones.sergio.readercollection.R
import com.getkeepsafe.taptargetview.TapTarget

fun TapTarget.style(activity: Activity): TapTarget {
    return this
        .titleTextColor(R.color.textTertiary)
        .titleTextSize(activity.resources.getDimension(R.dimen.text_size_6sp).toInt())
        .titleTypeface(activity.getCustomFont(R.font.roboto_bold))
        .descriptionTextColor(R.color.textTertiary)
        .descriptionTextSize(activity.resources.getDimension(R.dimen.text_size_4sp).toInt())
        .descriptionTypeface(activity.getCustomFont(R.font.roboto_regular))
}