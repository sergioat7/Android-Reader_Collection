/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.viewholders.BooksViewHolder
import java.util.ArrayList

class BooksAdapter(
    var books: MutableList<BookResponse>,
    private val isGoogleBook: Boolean,
    private val context: Context,
    private var onItemClickListener: OnItemClickListener
): RecyclerView.Adapter<BooksViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {

        val itemView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.book_item,
            parent,
            false
        )
        return BooksViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {

        val book = books[position]
        holder.fillData(
            book,
            isGoogleBook,
            context
        )

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(book.id)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(bookId: String)
    }

    //MARK: - Public methods

    fun addBooks(newBooks: MutableList<BookResponse>) {

        val position: Int = this.books.size
        this.books = newBooks
        notifyItemInserted(position)
    }

    fun resetList() {

        this.books = ArrayList<BookResponse>()
        notifyDataSetChanged()
    }
}