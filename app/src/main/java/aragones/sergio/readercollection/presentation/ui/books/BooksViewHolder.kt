/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ItemBookBinding
import aragones.sergio.readercollection.databinding.ItemReadingBookBinding
import aragones.sergio.readercollection.databinding.ItemVerticalBookBinding
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener
import aragones.sergio.readercollection.presentation.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.presentation.interfaces.OnSwitchClickListener
import kotlin.math.ceil

class BooksViewHolder(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    @SuppressLint("ClickableViewAccessibility")
    fun bind(
        book: Book,
        isGoogleBook: Boolean,
        isDraggingEnable: Boolean,
        isFirst: Boolean,
        isLast: Boolean,
        onItemClickListener: OnItemClickListener,
        onStartDraggingListener: OnStartDraggingListener?,
        onSwitchClickListener: OnSwitchClickListener?
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
                    this.isSwitchLeftIconEnabled = !isFirst && book.isPending()
                    this.isSwitchRightIconEnabled = !isLast && book.isPending()
                    this.imageViewSwitchLeft.setOnClickListener {
                        onSwitchClickListener?.onSwitchLeft(adapterPosition)
                    }
                    this.imageViewSwitchRight.setOnClickListener {
                        onSwitchClickListener?.onSwitchRight(adapterPosition)
                    }
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
                            onStartDraggingListener?.onStartDragging(this@BooksViewHolder)
                        }
                        false
                    }
                }

                else -> Unit
            }
        }
    }

    fun setSelected(isSelected: Boolean) {

        val colorId = if (isSelected) R.color.colorQuaternary else R.color.colorSecondary
        when (binding) {
            is ItemVerticalBookBinding -> {
                binding.constraintLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        colorId
                    )
                )
            }

            is ItemBookBinding -> {
                binding.constraintLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        colorId
                    )
                )
            }

            else -> Unit
        }
    }
    //endregion
}