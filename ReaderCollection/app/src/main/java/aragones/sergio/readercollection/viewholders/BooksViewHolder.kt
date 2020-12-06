/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.viewholders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_item.view.*

class BooksViewHolder(
itemView: View
) : RecyclerView.ViewHolder(itemView) {

    //MARK: - Public methods

    fun fillData(book: BookResponse, isGoogleBook: Boolean, context: Context) {

        val image = book.thumbnail?.replace("http", "https") ?: "-"
        val errorImage = if (Constants.isDarkMode(context)) R.drawable.default_book_cover_dark else R.drawable.default_book_cover_light
        val loading = itemView.progress_bar_loading
        loading.visibility = View.VISIBLE
        Picasso
            .get()
            .load(image)
            .fit()
            .centerCrop()
            .error(errorImage)
            .into(itemView.image_view_book, object: Callback {

                override fun onSuccess() {
                    loading.visibility = View.GONE
                }

                override fun onError(e: Exception) {
                    loading.visibility = View.GONE
                }
            })

        itemView.text_view_title.text = book.title

        val authors = book.authors?.joinToString(separator = ", ") ?: ""
        itemView.text_view_author.text = context.resources.getString(R.string.authors_text, authors)
        itemView.text_view_author.visibility = if(authors.isBlank()) View.GONE else View.VISIBLE

        val publisher = book.publisher ?: ""
        itemView.text_view_publisher.text = context.resources.getString(R.string.publisher_text, publisher)
        itemView.text_view_publisher.visibility = if(publisher.isBlank()) View.GONE else View.VISIBLE

        val rating = if (isGoogleBook) book.averageRating else book.rating
        itemView.rating_bar.rating = rating.toFloat() / 2
        itemView.text_view_rating.text = rating.toInt().toString()
        itemView.linear_layout_rating.visibility = if (rating > 0) View.VISIBLE else View.GONE
        itemView.text_view_new.visibility = if (rating > 0) View.GONE else View.VISIBLE
    }
}