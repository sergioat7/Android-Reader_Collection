/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/11/2020
 */

package aragones.sergio.readercollection.viewholders

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.OnItemClickListener

class LoadMoreItemsViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    //region Public methods
    fun setItem(onItemClickListener: OnItemClickListener) {

        val btLoadMoreItems = itemView.findViewById<Button>(R.id.button_load_more_items)
        btLoadMoreItems.setOnClickListener {
            onItemClickListener.onLoadMoreItemsClick()
        }
    }
    //endregion
}