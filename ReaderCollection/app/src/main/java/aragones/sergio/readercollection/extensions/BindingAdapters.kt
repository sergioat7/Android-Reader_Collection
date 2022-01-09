/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.res.ColorStateList
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import me.zhanghai.android.materialratingbar.MaterialRatingBar

@BindingAdapter(value = ["isEnabled", "backgroundTint"], requireAll = false)
fun setEnabled(spinner: Spinner, isEnabled: Boolean?, backgroundTint: Int?) {
    spinner.isEnabled = isEnabled == true
    backgroundTint?.let {
        spinner.backgroundTintList = ColorStateList.valueOf(backgroundTint)
    }
}

@BindingAdapter("rating")
fun setRating(ratingBar: MaterialRatingBar, rating: Double?) {
    ratingBar.rating = rating?.toFloat() ?: 0F
}