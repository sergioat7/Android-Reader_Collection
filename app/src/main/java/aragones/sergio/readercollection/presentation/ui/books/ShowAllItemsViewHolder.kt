/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.books

import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.databinding.ItemShowAllItemsBinding
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener

class ShowAllItemsViewHolder(private val binding: ItemShowAllItemsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(state: String, onItemClickListener: OnItemClickListener) {

        binding.apply {
            this.state = state
            this.onItemClickListener = onItemClickListener
        }
    }
    //endregion
}