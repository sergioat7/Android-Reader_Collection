/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.extensions.getCustomFont

class MenuAdapter(
    private val ctx: Context,
    val values: List<CharSequence>,
    layoutId: Int = android.R.layout.simple_dropdown_item_1line,
) : ArrayAdapter<Any?>(ctx, layoutId, values) {

    //region Lifecycle methods
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item: View = convertView
            ?: LayoutInflater
                .from(ctx)
                .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)

        item.findViewById<TextView>(android.R.id.text1).apply {
            text = values[position]
            typeface = ctx.getCustomFont(R.font.roboto_serif_regular)
        }
        return item
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        super.getDropDownView(position, convertView, parent)
    //endregion
}