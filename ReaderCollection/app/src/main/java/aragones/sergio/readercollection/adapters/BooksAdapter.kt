/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.State
import aragones.sergio.readercollection.viewholders.BooksViewHolder
import aragones.sergio.readercollection.viewholders.LoadMoreItemsViewHolder
import java.util.*

class BooksAdapter(
    private var books: MutableList<BookResponse>,
    private val isGoogleBook: Boolean,
    private val context: Context,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    //region Lifecycle methods
    override fun getItemViewType(position: Int): Int {

        val book = books[position]
        return when {
            book.state == State.READING -> R.layout.item_reading_book
            isGoogleBook -> R.layout.item_google_book
            book.id.isNotBlank() -> R.layout.item_book
            else -> R.layout.item_load_more_items
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View = LayoutInflater.from(parent.context).inflate(
            viewType,
            parent,
            false
        )
        return when (viewType) {
            R.layout.item_reading_book, R.layout.item_google_book, R.layout.item_book -> BooksViewHolder(
                itemView
            )
            else -> LoadMoreItemsViewHolder(itemView)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is BooksViewHolder) {
            val book = books[position]
            when {
                book.state == State.READING -> holder.fillReadingData(book, context)
                isGoogleBook -> holder.fillGoogleData(book, context)
                else -> holder.fillData(book, context)
            }

            holder.itemView.setOnClickListener {
                onItemClickListener.onItemClick(book.id)
            }
        } else {
            (holder as LoadMoreItemsViewHolder).setItem(onItemClickListener)
        }
    }
    //endregion

    //region Public methods
    @SuppressLint("NotifyDataSetChanged")
    fun setBooks(newBooks: MutableList<BookResponse>) {

        val position = this.books.size
        this.books = newBooks
        if (position < newBooks.size) {
            notifyItemInserted(position)
        } else {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetList() {

        this.books = ArrayList<BookResponse>()
        notifyDataSetChanged()
    }
    //endregion
}