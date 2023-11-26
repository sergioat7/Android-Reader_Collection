/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.ui.books

import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ItemBookBinding
import aragones.sergio.readercollection.databinding.ItemReadingBookBinding
import aragones.sergio.readercollection.databinding.ItemVerticalBookBinding
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.models.BookResponse
import kotlin.math.ceil

class BooksViewHolder(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(
        book: BookResponse,
        isGoogleBook: Boolean,
        isDraggingEnable: Boolean,
        onItemClickListener: OnItemClickListener,
        onStartDraggingListener: OnStartDraggingListener?
    ) {
        binding.apply {
            when (this) {

                is ItemReadingBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                    this.isDarkMode = binding.root.context.isDarkMode()
                }

                is ItemVerticalBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                    this.isDarkMode = binding.root.context.isDarkMode()
                }

                is ItemBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener
                    this.isGoogleBook = isGoogleBook
                    this.isDarkMode = binding.root.context.isDarkMode()
                    this.isDraggingEnable = isDraggingEnable
                    val rating = if (isGoogleBook) book.averageRating else book.rating
                    textViewGoogleBookRating.text = ceil(rating).toInt().toString()
                    this.imageViewDragging.setOnTouchListener { _, event ->

                        if (event.action == MotionEvent.ACTION_DOWN) {
                            onStartDraggingListener?.requestDrag(this@BooksViewHolder)
                        }
                        false
                    }
                }

                else -> Unit
            }
        }
    }

    fun setSelected(isSelected: Boolean) {

        if (binding is ItemBookBinding) {
            val colorId = if (isSelected) R.color.colorQuaternary else R.color.colorSecondary
            binding.constraintLayout.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    colorId
                )
            )
        }
    }
    //endregion
}