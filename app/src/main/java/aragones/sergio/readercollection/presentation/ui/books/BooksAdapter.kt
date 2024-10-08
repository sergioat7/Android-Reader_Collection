/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ItemBookBinding
import aragones.sergio.readercollection.databinding.ItemLoadMoreItemsBinding
import aragones.sergio.readercollection.databinding.ItemReadingBookBinding
import aragones.sergio.readercollection.databinding.ItemShowAllItemsBinding
import aragones.sergio.readercollection.databinding.ItemVerticalBookBinding
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.interfaces.ItemMoveListener
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener
import aragones.sergio.readercollection.presentation.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.presentation.interfaces.OnSwitchClickListener
import com.aragones.sergio.util.Constants
import java.util.Collections

class BooksAdapter(
    private var books: MutableList<Book>,
    private val isVerticalDesign: Boolean,
    private val isGoogleBook: Boolean,
    private var onItemClickListener: OnItemClickListener,
    private var onStartDraggingListener: OnStartDraggingListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>(), ItemMoveListener, OnSwitchClickListener {

    //region Private properties
    private lateinit var recyclerView: RecyclerView
    private var position = 0
    private var isDraggingEnabled = false
    private var isSwitchingEnabled = false
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
            is BooksViewHolder -> {

                val isFirst = position == 0 || !isSwitchingEnabled
                val isLast =
                    position == Constants.BOOKS_TO_SHOW - 1 || position == books.count() - 1 || !isSwitchingEnabled
                holder.bind(
                    books[position],
                    isGoogleBook,
                    isDraggingEnabled,
                    isFirst,
                    isLast,
                    onItemClickListener,
                    onStartDraggingListener,
                    this
                )
            }

            is ShowAllItemsViewHolder -> holder.bind(books.first().state ?: "", onItemClickListener)
            else -> (holder as LoadMoreItemsViewHolder).bind(onItemClickListener)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }
    //endregion

    //region Public methods
    @SuppressLint("NotifyDataSetChanged")
    fun setBooks(newBooks: MutableList<Book>, reset: Boolean) {

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
        this.books = ArrayList<Book>()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDragging(enable: Boolean) {

        isDraggingEnabled = enable
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSwitching(enable: Boolean) {

        isSwitchingEnabled = enable
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
        recyclerView.scrollToPosition(toPosition)
        onStartDraggingListener?.onFinishDragging(listOf(books[fromPosition], books[toPosition]))
    }

    override fun onSwitchRight(fromPosition: Int) {

        val toPosition = fromPosition + 1
        books[fromPosition].priority = toPosition
        books[toPosition].priority = fromPosition
        onRowMoved(fromPosition, toPosition)
        recyclerView.scrollToPosition(toPosition)
        onStartDraggingListener?.onFinishDragging(listOf(books[fromPosition], books[toPosition]))
    }
    //endregion
}