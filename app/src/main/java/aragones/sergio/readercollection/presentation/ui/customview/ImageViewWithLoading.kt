/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ImageViewWithLoadingBinding

class ImageViewWithLoading @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding: ImageViewWithLoadingBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.image_view_with_loading,
        this,
        true
    )
}