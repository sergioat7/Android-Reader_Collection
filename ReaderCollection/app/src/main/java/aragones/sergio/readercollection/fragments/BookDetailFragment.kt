/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.extensions.showDatePicker
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
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
    private lateinit var svCategories: HorizontalScrollView
    private lateinit var llCategories: LinearLayout
    private lateinit var tvDescription: TextView
    private lateinit var btReadMoreDescription: Button
    private lateinit var tvIsbn: TextView
    private lateinit var tvPageCount: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvPublishedDate: TextView
    private lateinit var llTitles3: LinearLayout
    private lateinit var llValues3: LinearLayout
    private lateinit var etReadingDate: EditText
    private lateinit var llTitles4: LinearLayout
    private lateinit var llValues4: LinearLayout
    private lateinit var spFormats: Spinner
    private lateinit var spStates: Spinner
    private lateinit var llSummary: LinearLayout
    private lateinit var tvSummary: TextView
    private lateinit var btReadMoreSummary: Button
    private lateinit var viewModel: BookDetailViewModel
    private var isFavourite: Boolean = false
    private lateinit var sharedPreferencesHandler: SharedPreferencesHandler

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

        ivBook = image_view_book
        pbLoadingImage = progress_bar_loading
        fbFavourite = floating_action_button_favourite
        llRating = linear_layout_rating
        rbStars = rating_bar
        tvRatingCount = text_view_rating_count
        tvNoRatings = text_view_no_ratings
        tvTitle = text_view_title
        tvAuthor = text_view_author
        svCategories = horizontal_scroll_view_categories
        llCategories = linear_layout_categories
        tvDescription = text_view_description
        btReadMoreDescription = button_read_more_description
        tvIsbn = text_view_isbn
        tvPageCount = text_view_page_count
        tvPublisher = text_view_publisher
        tvPublishedDate = text_view_published_date
        llTitles3 = linear_layout_titles_3
        llValues3 = linear_layout_values_3
        etReadingDate = edit_text_reading_date
        llTitles4 = linear_layout_titles_4
        llValues4 = linear_layout_values_4
        spFormats = spinner_formats
        spStates = spinner_states
        llSummary = linear_layout_summary
        tvSummary = text_view_summary
        btReadMoreSummary = button_read_more_summary
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BookDetailViewModelFactory(application)).get(
            BookDetailViewModel::class.java
        )
        viewModel.setBookId(bookId)
        viewModel.setIsGoogleBook(isGoogleBook)
        sharedPreferencesHandler = SharedPreferencesHandler(
            context?.getSharedPreferences(
                Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
        )
        setupBindings()

        etReadingDate.showDatePicker(requireContext())

        spFormats.adapter = Constants.getAdapter(requireContext(), ArrayList())//TODO send format values
        spStates.adapter = Constants.getAdapter(requireContext(), ArrayList())//TODO send state values

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

        var authors = Constants.listToString(book.authors)
        if (authors.isEmpty()) {
            authors = Constants.NO_VALUE
        }
        tvAuthor.text = resources.getString(R.string.authors_text, authors)

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
        svCategories.visibility = if (llCategories.childCount > 0) View.VISIBLE else View.GONE

        var description = Constants.NO_VALUE
        if (book.description != null && book.description.isNotBlank()) {
            description = book.description
        }
        tvDescription.text = description

        btReadMoreDescription.visibility = if(description == Constants.NO_VALUE) View.GONE else View.VISIBLE
        btReadMoreDescription.setOnClickListener {

            tvDescription.maxLines = Constants.MAX_LINES
            btReadMoreDescription.visibility = View.GONE
        }

        var summary = Constants.NO_VALUE
        if (book.summary != null && book.summary.isNotBlank()) {
            summary = book.summary
        }
        tvSummary.text = summary

        llSummary.visibility = if(isGoogleBook) View.GONE else View.VISIBLE

        btReadMoreSummary.visibility = if(summary == Constants.NO_VALUE) View.GONE else View.VISIBLE
        btReadMoreSummary.setOnClickListener {

            tvSummary.maxLines = Constants.MAX_LINES
            btReadMoreSummary.visibility = View.GONE
        }

        //TODO set format

        //TODO set state

        llTitles4.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
        llValues4.visibility = if(isGoogleBook) View.GONE else View.VISIBLE

        var isbn = Constants.NO_VALUE
        if (book.isbn != null && book.isbn.isNotBlank()) {
            isbn = book.isbn
        }
        tvIsbn.text = isbn

        tvPageCount.text = book.pageCount.toString()

        var publisher = Constants.NO_VALUE
        if (book.publisher != null && book.publisher.isNotBlank()) {
            publisher = book.publisher
        }
        tvPublisher.text = publisher

        var publishedDate = Constants.dateToString(book.publishedDate, Constants.getDateFormatToShow(sharedPreferencesHandler))
        if (publishedDate == null || publishedDate.isEmpty()) {
            publishedDate = Constants.NO_VALUE
        }
        tvPublishedDate.text = publishedDate

        var readingDate = Constants.dateToString(book.readingDate, Constants.getDateFormatToShow(sharedPreferencesHandler))
        if (readingDate == null || readingDate.isEmpty()) {
            readingDate = Constants.NO_VALUE
        }
        etReadingDate.setText(readingDate)


        llTitles3.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
        llValues3.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
    }
}