/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import aragones.sergio.readercollection.customview.ImageViewWithLoading
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import me.zhanghai.android.materialratingbar.MaterialRatingBar

@BindingAdapter(
    value = ["src", "center", "placeholder", "radius", "loadingColor"],
    requireAll = false
)
fun setImageUri(
    imageViewWithLoading: ImageViewWithLoading,
    image: String?,
    center: Boolean?,
    placeholder: Drawable?,
    radius: Float?,
    loadingColor: Int?
) {

    loadingColor?.let {
        imageViewWithLoading.binding.progressBarImageLoading.indeterminateTintList =
            ColorStateList.valueOf(it)
    }
    val img = image?.replace("http:", "https:") ?: "-"
    Picasso
        .get()
        .load(img)
        .apply {
            if (center == true) {
                fit().centerCrop()
            }
            placeholder?.let {
                error(it)
            }
        }
        .into(imageViewWithLoading.binding.imageView, object : Callback {

            override fun onSuccess() {
                if (radius != null && radius >= 0) {
                    imageViewWithLoading.binding.imageView.apply {
                        this.setImageDrawable(this.getRoundImageView(radius))
                    }
                }
                imageViewWithLoading.binding.progressBarImageLoading.visibility = View.GONE
            }

            override fun onError(e: Exception) {
                imageViewWithLoading.binding.progressBarImageLoading.visibility = View.GONE
            }
        })
}

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