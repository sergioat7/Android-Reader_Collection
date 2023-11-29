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
import aragones.sergio.readercollection.interfaces.ItemMoveListener
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.interfaces.OnSwitchClickListener
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import java.util.*

class BooksAdapter(
    private var books: MutableList<BookResponse>,
    private val isVerticalDesign: Boolean,
    private val isGoogleBook: Boolean,
    private var onItemClickListener: OnItemClickListener,
    private var onStartDraggingListener: OnStartDraggingListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>(), ItemMoveListener, OnSwitchClickListener {

    //region Private properties
    private var position = 0
    private var isDraggingEnabled = false
    //endregion

    //region Lifecycle methods
    override fun getItemViewType(position: Int): Int {

        val book = books[position]
        return when {
            book.isReading() -> R.layout.item_reading_book
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
            is BooksViewHolder -> holder.bind(
                books[position],
                isGoogleBook,
                isDraggingEnabled,
                position == 0,
                position == Constants.BOOKS_TO_SHOW - 1,
                onItemClickListener,
                onStartDraggingListener,
                this
            )

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

    @SuppressLint("NotifyDataSetChanged")
    fun setDragging(enable: Boolean) {

        isDraggingEnabled = enable
        notifyDataSetChanged()
    }
    //endregion

    //region Interface methods
    override fun onRowMoved(fromPosition: Int, toPosition: Int) {

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(books, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(books, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: BooksViewHolder) {
        myViewHolder.setSelected(true)
    }

    override fun onRowClear(myViewHolder: BooksViewHolder) {

        myViewHolder.setSelected(false)
        for ((index, book) in books.withIndex()) {
            book.priority = index
        }
        onStartDraggingListener?.onFinishDragging(books)
    }

    override fun onSwitchLeft(fromPosition: Int) {

        val toPosition = fromPosition - 1
        books[fromPosition].priority = toPosition
        books[toPosition].priority = fromPosition
        onRowMoved(fromPosition, toPosition)
        onStartDraggingListener?.onFinishDragging(listOf(books[fromPosition], books[toPosition]))
    }

    override fun onSwitchRight(fromPosition: Int) {

        val toPosition = fromPosition + 1
        books[fromPosition].priority = toPosition
        books[toPosition].priority = fromPosition
        onRowMoved(fromPosition, toPosition)
        onStartDraggingListener?.onFinishDragging(listOf(books[fromPosition], books[toPosition]))
    }
    //endregion
}