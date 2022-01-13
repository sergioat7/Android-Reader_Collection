/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.View
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import aragones.sergio.readercollection.customview.ImageViewWithLoading
import aragones.sergio.readercollection.utils.CustomInputType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

@BindingAdapter("end_icon_mode")
fun setEndIconMode(textInputLayout: TextInputLayout, mode: Int?) {
    mode?.let {
        when (it > 0) {
            true -> textInputLayout.endIconMode = it
            false -> Unit
        }
    }
}

@BindingAdapter("clickable")
fun setClickable(textInputLayout: TextInputLayout, clickable: Boolean?) {
    textInputLayout.isHovered = clickable != true
}

@BindingAdapter("customInputType")
fun setInputType(view: TextInputEditText, inputType: CustomInputType?) {
    view.inputType = when (inputType) {
        CustomInputType.TEXT -> InputType.TYPE_CLASS_TEXT
        CustomInputType.MULTI_LINE_TEXT -> InputType.TYPE_CLASS_TEXT// or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        CustomInputType.NUMBER -> InputType.TYPE_CLASS_NUMBER
        CustomInputType.PASSWORD -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        else -> InputType.TYPE_NULL
    }
}