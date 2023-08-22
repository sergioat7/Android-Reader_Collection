/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.books

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.*
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.utils.State
import java.util.*

class BooksAdapter(
    private var books: MutableList<BookResponse>,
    private val isVerticalDesign: Boolean,
    private val isGoogleBook: Boolean,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    //region Private properties
    private var position = 0
    //endregion

    //region Lifecycle methods
    override fun getItemViewType(position: Int): Int {

        val book = books[position]
        return when {
            book.state == State.READING -> R.layout.item_reading_book
            isVerticalDesign && book.id.isNotBlank() -> R.layout.item_vertical_book
            !isVerticalDesign && book.id.isNotBlank() -> R.layout.item_book
            isVerticalDesign -> R.layout.item_show_all_items
            else -> R.layout.item_load_more_items
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            R.layout.item_reading_book -> {
                BooksViewHolder(
                    ItemReadingBookBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.item_vertical_book -> {
                BooksViewHolder(
                    ItemVerticalBookBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.item_book -> {
                BooksViewHolder(
                    ItemBookBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.item_show_all_items -> {
                ShowAllItemsViewHolder(
                    ItemShowAllItemsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                LoadMoreItemsViewHolder(
                    ItemLoadMoreItemsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is BooksViewHolder -> holder.bind(books[position], isGoogleBook, onItemClickListener)
            is ShowAllItemsViewHolder -> holder.bind(books.first().state ?: "", onItemClickListener)
            else -> (holder as LoadMoreItemsViewHolder).bind(onItemClickListener)
        }
    }
    //endregion

    //region Public methods
    @SuppressLint("NotifyDataSetChanged")
    fun setBooks(newBooks: MutableList<BookResponse>, reset: Boolean) {

        if (reset) {
            resetList()
        }
        this.books = newBooks
        if (position < newBooks.size) {
            notifyItemInserted(position)
        } else {
            notifyDataSetChanged()
        }
        position = this.books.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetList() {

        position = 0
        this.books = ArrayList<BookResponse>()
        notifyDataSetChanged()
    }
    //endregion
}