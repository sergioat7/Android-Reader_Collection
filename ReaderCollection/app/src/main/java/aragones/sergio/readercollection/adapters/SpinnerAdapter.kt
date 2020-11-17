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
    private val values: List<String>
): ArrayAdapter<Any?>(ctx, R.layout.spinner_dropdown_item, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listItem = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.spinner_item, parent, false)

        val tvValue = listItem.findViewById<TextView>(R.id.text_view_value)
        tvValue.text = values[position]

        val colorId = if(position == 0) R.color.textSecondaryLight else R.color.textSecondary
        tvValue.setTextColor(ContextCompat.getColor(ctx, colorId))

        return listItem
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {

        val tvValue = super.getDropDownView(position, convertView, parent) as TextView
        val colorId = if(position == 0) R.color.textSecondaryLight else R.color.textSecondary
        tvValue.setTextColor(ContextCompat.getColor(ctx, colorId))
        return tvValue
    }
}