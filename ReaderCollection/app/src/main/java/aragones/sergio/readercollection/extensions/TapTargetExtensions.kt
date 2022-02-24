/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/2/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.Context
import aragones.sergio.readercollection.R
import com.getkeepsafe.taptargetview.TapTarget

fun TapTarget.style(context: Context, oppositeTheme: Boolean? = false): TapTarget {
    return this
        .outerCircleColor(if (oppositeTheme == true) R.color.colorSecondary else R.color.colorPrimary)
        .titleTextColor(if (oppositeTheme == true) R.color.colorPrimary else R.color.colorSecondary)
        .titleTextSize(context.resources.getDimension(R.dimen.text_size_6sp).toInt())
        .titleTypeface(context.getCustomFont(R.font.roboto_serif_bold))
        .descriptionTextColor(if (oppositeTheme == true) R.color.colorPrimary else R.color.colorSecondary)
        .descriptionTextSize(context.resources.getDimension(R.dimen.text_size_4sp).toInt())
        .descriptionTypeface(context.getCustomFont(R.font.roboto_serif_regular))
        .targetCircleColor(R.color.colorTertiary)
}