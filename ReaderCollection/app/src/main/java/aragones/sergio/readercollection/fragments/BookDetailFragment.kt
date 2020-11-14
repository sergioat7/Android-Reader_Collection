/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.book_detail_fragment.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class BookDetailFragment: BaseFragment() {

    //MARK: - Private properties

    private var bookId: String = ""
    private var isGoogleBook: Boolean = false
    private lateinit var ivBook: ImageView
    private lateinit var pbLoadingImage: ProgressBar
    private lateinit var fbFavourite: FloatingActionButton
    private lateinit var llRating: LinearLayout
    private lateinit var rbStars: MaterialRatingBar
    private lateinit var tvRatingCount: TextView
    private lateinit var tvNoRatings: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var llCategories: LinearLayout
    private lateinit var tvDescription: TextView
    private lateinit var btReadMore: Button
    private lateinit var viewModel: BookDetailViewModel
    private var isFavourite: Boolean = false

    //MARK: - Lifecycle methods

    companion object {
        fun newInstance() = BookDetailFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        bookId = this.arguments?.getString(Constants.BOOK_ID) ?: ""
        isGoogleBook = this.arguments?.getBoolean(Constants.IS_GOOGLE_BOOK) ?: false
        return inflater.inflate(R.layout.book_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.book_detail_toolbar_menu, menu)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BookDetailViewModelFactory(application)).get(
            BookDetailViewModel::class.java
        )
        viewModel.setBookId(bookId)
        viewModel.setIsGoogleBook(isGoogleBook)
        ivBook = image_view_book
        pbLoadingImage = progress_bar_loading
        fbFavourite = floating_action_button_favourite
        llRating = linear_layout_rating
        rbStars = rating_bar
        tvRatingCount = text_view_rating_count
        tvNoRatings = text_view_no_ratings
        tvTitle = text_view_title
        tvAuthor = text_view_author
        llCategories = linear_layout_categories
        tvDescription = text_view_description
        btReadMore = button_read_more
        setupBindings()

        viewModel.getBook()
    }

    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner, {
            showData(it)
        })

        viewModel.bookDetailLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.bookDetailError.observe(viewLifecycleOwner, {
            manageError(it)
        })
    }

    private fun showData(book: BookResponse) {

        val image = book.image?.replace("http", "https") ?: "-"
        Picasso
            .get()
            .load(image)
            .fit()
            .centerCrop()
            .into(ivBook, object : Callback {

                override fun onSuccess() {
                    pbLoadingImage.visibility = View.GONE
                }

                override fun onError(e: Exception) {
                    pbLoadingImage.visibility = View.GONE
                }
            })

        isFavourite = book.isFavourite
        fbFavourite.setImageResource(Constants.getFavouriteImage(isFavourite, context))
        fbFavourite.setOnClickListener {

            isFavourite = !isFavourite
            fbFavourite.setImageResource(Constants.getFavouriteImage(isFavourite, context))
        }

        val rating = if (isGoogleBook) book.averageRating else book.rating
        rbStars.rating = rating.toFloat() / 2
        rbStars.setIsIndicator(isGoogleBook)

        tvRatingCount.text = book.ratingsCount.toString()

        val hideRating = rating == 0.0 && isGoogleBook
        llRating.visibility = if (hideRating) View.INVISIBLE else View.VISIBLE
        tvNoRatings.visibility = if (hideRating) View.VISIBLE else View.GONE

        tvTitle.text = book.title

        tvAuthor.text = resources.getString(R.string.authors_text, Constants.listToString(book.authors))

        llCategories.removeAllViews()
        book.categories?.let { categories ->
            for (category in categories) {

                val tv = Constants.getRoundedTextView(category, requireContext())
                llCategories.addView(tv)

                val view = View(context)
                view.layoutParams = ViewGroup.LayoutParams(
                    20,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                llCategories.addView(view)
            }
        }

        tvDescription.text = book.description

        btReadMore.setOnClickListener {

            tvDescription.maxLines = Constants.MAX_LINES
            btReadMore.visibility = View.GONE
        }
    }
}