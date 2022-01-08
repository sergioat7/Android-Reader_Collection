/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.viewholders

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.databinding.ItemBookBinding
import aragones.sergio.readercollection.databinding.ItemGoogleBookBinding
import aragones.sergio.readercollection.databinding.ItemReadingBookBinding
import aragones.sergio.readercollection.extensions.getRoundImageView
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class BooksViewHolder(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //region Public methods
    fun bind(book: BookResponse, onItemClickListener: OnItemClickListener) {
        binding.apply {
            when (this) {

                is ItemReadingBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener

                    val image = book.thumbnail?.replace("http", "https") ?: "-"
                    progressBarReadingImageLoading.visibility = View.VISIBLE
                    Picasso
                        .get()
                        .load(image)
                        .fit()
                        .centerCrop()
                        .error(R.drawable.ic_default_book_cover)//TODO: change cover
                        .into(imageViewReadingBook, object : Callback {

                            override fun onSuccess() {
                                imageViewReadingBook.apply {
                                    this.setImageDrawable(this.getRoundImageView(Constants.IMAGE_CORNER))
                                }
                                progressBarReadingImageLoading.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                progressBarReadingImageLoading.visibility = View.GONE
                            }
                        })

                    val authors = book.authors?.joinToString(separator = ", ") ?: ""
                    textViewReadingBookAuthor.text = authors
                    textViewReadingBookAuthor.visibility =
                        if (authors.isBlank()) View.GONE else View.VISIBLE

                }

                is ItemGoogleBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener

                    val image = book.thumbnail?.replace("http", "https") ?: "-"
                    progressBarGoogleImageLoading.visibility = View.VISIBLE
                    Picasso
                        .get()
                        .load(image)
                        .fit()
                        .centerCrop()
                        .error(R.drawable.ic_default_book_cover)
                        .into(imageViewGoogleBook, object : Callback {

                            override fun onSuccess() {
                                imageViewGoogleBook.apply {
                                    this.setImageDrawable(this.getRoundImageView(Constants.IMAGE_CORNER))
                                }
                                progressBarGoogleImageLoading.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                progressBarGoogleImageLoading.visibility = View.GONE
                            }
                        })

                    val authors = book.authors?.joinToString(separator = ", ") ?: ""
                    textViewGoogleBookAuthor.text =
                        binding.root.context.resources.getString(R.string.authors_text, authors)
                    textViewGoogleBookAuthor.visibility =
                        if (authors.isBlank()) View.GONE else View.VISIBLE

                    val rating = book.averageRating
                    ratingBarGoogleBook.rating = rating.toFloat() / 2
                    textViewGoogleBookRating.text = rating.toInt().toString()

                }

                is ItemBookBinding -> {
                    this.book = book
                    this.onItemClickListener = onItemClickListener

                    val image = book.thumbnail?.replace("http", "https") ?: "-"
                    progressBarImageLoading.visibility = View.VISIBLE
                    Picasso
                        .get()
                        .load(image)
                        .fit()
                        .centerCrop()
                        .error(R.drawable.ic_default_book_cover)
                        .into(imageViewBook, object : Callback {

                            override fun onSuccess() {
                                imageViewBook.apply {
                                    this.setImageDrawable(this.getRoundImageView(Constants.IMAGE_CORNER))
                                }
                                progressBarImageLoading.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                progressBarImageLoading.visibility = View.GONE
                            }
                        })

                    val authors = book.authors?.joinToString(separator = ", ") ?: ""
                    textViewAuthor.text = authors
                    textViewAuthor.visibility =
                        if (authors.isBlank()) View.GONE else View.VISIBLE

                }
                else -> Unit
            }
        }
    }
    //endregion
}