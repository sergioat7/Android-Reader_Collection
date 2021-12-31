/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/11/2020
 */

package aragones.sergio.readercollection.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import aragones.sergio.readercollection.R

class SpinnerAdapter(
    private val ctx: Context,
    private val values: List<String>,
    private val firstOptionEnabled: Boolean,
    private val rounded: Boolean,
    private val title: String?
): ArrayAdapter<Any?>(ctx, R.layout.item_spinner_dropdown, values) {

    //MARK: - Lifecycle methods

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layout = if (rounded) R.layout.item_spinner_rounded else R.layout.item_spinner
        val listItem = convertView ?: LayoutInflater.from(ctx).inflate(
            layout,
            parent,
            false
        )

        if (rounded) {
            val tvTitle = listItem.findViewById<TextView>(R.id.text_view_title)
            tvTitle.text = title
        }

        val tvValue = listItem.findViewById<TextView>(R.id.text_view_value)
        tvValue.text = values[position]

        val colorId = if (rounded) {
            if(position == 0 && !firstOptionEnabled) R.color.textTertiaryLight else R.color.textTertiary
        } else {
            if(position == 0 && !firstOptionEnabled) R.color.textSecondaryLight else R.color.textSecondary
        }
        tvValue.setTextColor(ContextCompat.getColor(ctx, colorId))

        return listItem
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 0 || firstOptionEnabled
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

        val tvValue = super.getDropDownView(position, convertView, parent) as TextView
        val colorId = if(position == 0 && !firstOptionEnabled) R.color.textSecondaryLight else R.color.textSecondary
        tvValue.setTextColor(ContextCompat.getColor(ctx, colorId))
        return tvValue
    }
}