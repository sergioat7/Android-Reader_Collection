/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.ui.books

import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.databinding.ItemShowAllItemsBinding

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