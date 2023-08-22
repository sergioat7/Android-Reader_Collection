/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/11/2020
 */

package aragones.sergio.readercollection.ui.books

import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.databinding.ItemLoadMoreItemsBinding

class LoadMoreItemsViewHolder(private val binding: ItemLoadMoreItemsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(onItemClickListener: OnItemClickListener) {
        binding.onItemClickListener = onItemClickListener
    }
    //endregion
}