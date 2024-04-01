/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/11/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.databinding.ItemLoadMoreItemsBinding
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener

class LoadMoreItemsViewHolder(private val binding: ItemLoadMoreItemsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(onItemClickListener: OnItemClickListener) {
        binding.onItemClickListener = onItemClickListener
    }
    //endregion
}