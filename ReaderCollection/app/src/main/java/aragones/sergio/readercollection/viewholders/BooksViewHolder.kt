/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.viewholders

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.databinding.ItemBookBinding
import aragones.sergio.readercollection.databinding.ItemGoogleBookBinding
import aragones.sergio.readercollection.databinding.ItemReadingBookBinding
import aragones.sergio.readercollection.models.responses.BookResponse
import kotlin.math.ceil

class BooksViewHolder(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(book: BookResponse, onItemClickListener: OnItemClickListener) {
        binding.apply {
            when (this) {

                is ItemReadingBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                }

                is ItemGoogleBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                    textViewGoogleBookRating.text = ceil(book.averageRating).toInt().toString()
                }

                is ItemBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                }
                else -> Unit
            }
        }
    }
    //endregion
}