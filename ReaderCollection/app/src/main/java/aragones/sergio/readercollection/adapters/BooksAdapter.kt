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
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewholders.BooksViewHolder
import aragones.sergio.readercollection.viewholders.LoadMoreItemsViewHolder
import java.util.*

class BooksAdapter(
    private var books: MutableList<BookResponse>,
    private val isGoogleBook: Boolean,
    private val context: Context,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    //MARK: - Lifecycle methods

    override fun getItemViewType(position: Int): Int {

        val book = books[position]
        return when {
            book.state == Constants.READING_STATE -> R.layout.reading_book_item
            isGoogleBook -> R.layout.google_book_item
            book.id.isNotBlank() -> R.layout.book_item
            else -> R.layout.load_more_items_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View = LayoutInflater.from(parent.context).inflate(
            viewType,
            parent,
            false
        )
        return when (viewType) {
            R.layout.reading_book_item, R.layout.google_book_item, R.layout.book_item -> BooksViewHolder(
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
                book.state == Constants.READING_STATE -> holder.fillReadingData(book, context)
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

    //MARK: - Public methods

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
}