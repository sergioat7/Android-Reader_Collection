/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/2/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import android.view.LayoutInflater
import aragones.sergio.readercollection.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun ChipGroup.addChip(inflater: LayoutInflater, text: String?) {
    (inflater.inflate(
        R.layout.content_chip,
        this,
        false
    ) as Chip).also { chip ->
        chip.text = text
        chip.setOnCloseIconClickListener {
            this.removeView(it)
        }
        this.addView(chip)
    }
}