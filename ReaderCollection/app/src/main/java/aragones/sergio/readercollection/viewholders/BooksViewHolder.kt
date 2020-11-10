/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.viewholders

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

    fun fillData(book: BookResponse, context: Context) {

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

        itemView.text_view_name.text = book.title

        val llCategories = itemView.linear_layout_categories
        llCategories.removeAllViews()
        book.categories?.let { categories ->
            for (category in categories) {

                val tv = getRoundedTextView(category, context)
                llCategories.addView(tv)

                val view = View(context)
                view.layoutParams = ViewGroup.LayoutParams(
                    20,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                llCategories.addView(view)
            }
        }
    }

    //MARK: - Private methods

    private fun getRoundedTextView(text: String, context: Context): TextView {

        val tv = TextView(context, null, R.style.RoundedTextView, R.style.RoundedTextView)
        tv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tv.text = text
        return tv
    }
}