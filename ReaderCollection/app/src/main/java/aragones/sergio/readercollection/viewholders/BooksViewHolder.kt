/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/11/2020
 */

package aragones.sergio.readercollection.viewholders

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_item.view.*

class BooksViewHolder(
itemView: View
) : RecyclerView.ViewHolder(itemView) {

    fun fillData(book: BookResponse, context: Context) {

        val loading = itemView.progress_bar_loading
        loading.visibility = View.VISIBLE
        Picasso
            .get()
            .load(book.thumbnail)
            .fit()
            .centerCrop()
//            .error(R.drawable.no_image)
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

                val tv = TextView(context)
                tv.text = category
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
}