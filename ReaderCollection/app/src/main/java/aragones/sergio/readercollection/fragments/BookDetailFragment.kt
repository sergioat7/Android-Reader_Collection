/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.extensions.setReadOnly
import aragones.sergio.readercollection.extensions.showDatePicker
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
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
    private lateinit var pbLoadingFavourite: ProgressBar
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
    private lateinit var llSummary: LinearLayout
    private lateinit var etSummary: EditText
    private lateinit var btReadMoreSummary: Button
    private lateinit var llTitles1: LinearLayout
    private lateinit var llValues1: LinearLayout
    private lateinit var pbLoadingFormats: ProgressBar
    private lateinit var spFormats: Spinner
    private lateinit var pbLoadingStates: ProgressBar
    private lateinit var spStates: Spinner
    private lateinit var tvIsbn: TextView
    private lateinit var tvPageCount: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvPublishedDate: TextView
    private lateinit var llTitles4: LinearLayout
    private lateinit var llValues4: LinearLayout
    private lateinit var etReadingDate: EditText
    private lateinit var viewModel: BookDetailViewModel
    private var isFavourite: Boolean = false
    private var book: BookResponse? = null
    private lateinit var formats: List<FormatResponse>
    private lateinit var formatValues: MutableList<String>
    private lateinit var states: List<StateResponse>
    private lateinit var stateValues: MutableList<String>
    private val goBack = MutableLiveData<Boolean>()
    private lateinit var menu: Menu

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

        this.menu = menu
        menu.clear()

        val menuRes = if(isGoogleBook) R.menu.google_book_detail_toolbar_menu else R.menu.book_detail_toolbar_menu
        inflater.inflate(menuRes, menu)
        menu.findItem(R.id.action_save).isVisible = isGoogleBook
        if(!isGoogleBook) {
            menu.findItem(R.id.action_cancel).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_save ->  {
                if (isGoogleBook) {
                    viewModel.createBook(getBookData())
                } else {

                    viewModel.setBook(getBookData())
                    setEdition(false)
                }
            }
            R.id.action_edit -> setEdition(true)
            R.id.action_remove -> {

                showPopupConfirmationDialog(R.string.book_remove_confirmation, acceptHandler = {
                    viewModel.deleteBook()
                })
            }
            R.id.action_cancel -> {

                setEdition(false)
                book?.let { showData(it) }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        ivBook = image_view_book
        pbLoadingImage = progress_bar_loading_image
        fbFavourite = floating_action_button_favourite
        pbLoadingFavourite = progress_bar_loading_favourite
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
        llSummary = linear_layout_summary
        etSummary = edit_text_summary
        btReadMoreSummary = button_read_more_summary
        llTitles1 = linear_layout_titles_1
        llValues1 = linear_layout_values_1
        pbLoadingFormats = progress_bar_loading_formats
        spFormats = spinner_formats
        pbLoadingStates = progress_bar_loading_states
        spStates = spinner_states
        tvIsbn = text_view_isbn
        tvPageCount = text_view_page_count
        tvPublisher = text_view_publisher
        tvPublishedDate = text_view_published_date
        llTitles4 = linear_layout_titles_4
        llValues4 = linear_layout_values_4
        etReadingDate = edit_text_reading_date
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BookDetailViewModelFactory(application, bookId, isGoogleBook)).get(BookDetailViewModel::class.java)
        setupBindings()
        formats = listOf()
        formatValues = mutableListOf()
        states = listOf()
        stateValues = mutableListOf()

        fbFavourite.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
        pbLoadingFavourite.visibility = View.GONE
        fbFavourite.setOnClickListener {
            viewModel.setFavourite(!isFavourite)
        }

        rbStars.setIsIndicator(true)

        btReadMoreDescription.setOnClickListener {

            tvDescription.maxLines = Constants.MAX_LINES
            btReadMoreDescription.visibility = View.GONE
        }

        etSummary.setReadOnly(true, InputType.TYPE_NULL, 0)

        btReadMoreSummary.setOnClickListener {

            etSummary.maxLines = Constants.MAX_LINES
            btReadMoreSummary.visibility = View.GONE
        }

        spFormats.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        spFormats.isEnabled = false

        spStates.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        spStates.isEnabled = false

        etReadingDate.setOnClickListener {
            etReadingDate.showDatePicker(requireActivity())
        }
        etReadingDate.isEnabled = false
        etReadingDate.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    }

    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner, {

            book = it
            showData(it)
        })

        viewModel.isFavourite.observe(viewLifecycleOwner, {

            isFavourite = it
            fbFavourite.setImageResource(Constants.getFavouriteImage(isFavourite, context))
        })

        viewModel.formats.observe(viewLifecycleOwner, { formatsResponse ->

            formats = formatsResponse
            formatValues = mutableListOf()
            formatValues.run {

                this.add(resources.getString((R.string.select_format)))
                this.addAll(formatsResponse.map { it.name })
            }
            spFormats.adapter = Constants.getAdapter(requireContext(), formatValues)
            book?.let {
                setFormat(it)
            }
        })

        viewModel.states.observe(viewLifecycleOwner, { statesResponse ->

            states = statesResponse
            stateValues = mutableListOf()
            stateValues.run {

                this.add(resources.getString((R.string.select_state)))
                this.addAll(statesResponse.map { it.name })
            }
            spStates.adapter = Constants.getAdapter(requireContext(), stateValues)
            book?.let {
                setState(it)
            }
        })

        viewModel.bookDetailLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.bookDetailFormatsLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingFormats.visibility = if(isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookDetailStatesLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingStates.visibility = if(isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookDetailFavouriteLoading.observe(viewLifecycleOwner, { isLoading ->

            fbFavourite.visibility = if(isLoading) View.INVISIBLE else View.VISIBLE
            pbLoadingFavourite.visibility = if(isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookDetailSuccessMessage.observe(viewLifecycleOwner, {

            val message = resources.getString(it)
            showPopupDialog(message, goBack)
        })

        viewModel.bookDetailError.observe(viewLifecycleOwner, {
            manageError(it)
        })

        goBack.observe(viewLifecycleOwner, {
            activity?.onBackPressed()
        })
    }

    private fun showData(book: BookResponse) {

        val image =
            book.image?.replace("http", "https") ?:
            book.thumbnail?.replace("http", "https") ?: "-"
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

        val rating = if (isGoogleBook) book.averageRating else book.rating
        rbStars.rating = rating.toFloat() / 2

        tvRatingCount.text = book.ratingsCount.toString()

        val hideRating = rating == 0.0 && isGoogleBook
        llRating.visibility = if (hideRating) View.INVISIBLE else View.VISIBLE
        tvNoRatings.visibility = if (hideRating) View.VISIBLE else View.GONE

        tvTitle.text = StringBuilder()
            .append(book.title ?: "")
            .append(" ")
            .append(book.subtitle ?: "")
            .toString()

        val authors = Constants.listToString(book.authors)
        tvAuthor.visibility = if(authors.isEmpty()) View.GONE else View.VISIBLE
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

        btReadMoreDescription.visibility =
            if(description == Constants.NO_VALUE || tvDescription.maxLines == Constants.MAX_LINES) {
                View.GONE
            } else {
                View.VISIBLE
            }

        var summary = book.summary
        if (summary == null || summary.isBlank()) {
            summary = Constants.NO_VALUE
        }
        etSummary.setText(summary)

        llSummary.visibility = if(isGoogleBook) View.GONE else View.VISIBLE

        btReadMoreSummary.visibility =
            if(summary == Constants.NO_VALUE || etSummary.maxLines == Constants.MAX_LINES) {
                View.GONE
            } else {
                View.VISIBLE
            }

        setFormat(book)

        setState(book)

        llTitles1.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
        llValues1.visibility = if(isGoogleBook) View.GONE else View.VISIBLE

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

        var publishedDate = Constants.dateToString(
            book.publishedDate,
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        if (publishedDate == null || publishedDate.isBlank()) {
            publishedDate = Constants.NO_VALUE
        }
        tvPublishedDate.text = publishedDate

        var readingDate = Constants.dateToString(
            book.readingDate,
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        if (readingDate == null || readingDate.isBlank()) {
            readingDate = Constants.NO_VALUE
        }
        etReadingDate.setText(readingDate)

        llTitles4.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
        llValues4.visibility = if(isGoogleBook) View.GONE else View.VISIBLE
    }

    private fun setFormat(book: BookResponse) {

        var formatPosition = 0
        book.format?.let { formatId ->

            val formatName = formats.firstOrNull { it.id == formatId }?.name
            val pos = formatValues.indexOf(formatName)
            formatPosition = if(pos > 0) pos else 0
        }
        spFormats.setSelection(formatPosition)
    }

    private fun setState(book: BookResponse) {

        var statePosition = 0
        book.state?.let { stateId ->

            val stateName = states.firstOrNull { it.id == stateId }?.name
            val pos = stateValues.indexOf(stateName)
            statePosition = if(pos > 0) pos else 0
        }
        spStates.setSelection(statePosition)
    }

    private fun getBookData(): BookResponse {

        val summary = etSummary.text.toString()
        val readingDate = Constants.stringToDate(
            etReadingDate.text.toString(),
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        val rating = rbStars.rating.toDouble() * 2
        val format = viewModel.formats.value?.firstOrNull { it.name == spFormats.selectedItem.toString() }?.id
        val state = viewModel.states.value?.firstOrNull { it.name == spStates.selectedItem.toString() }?.id

        return BookResponse(
            id = book?.id ?: "",
            title = book?.title,
            subtitle = book?.subtitle,
            authors = book?.authors,
            publisher = book?.publisher,
            publishedDate = book?.publishedDate,
            readingDate = readingDate,
            description = book?.description,
            summary = summary,
            isbn = book?.isbn,
            pageCount = book?.pageCount ?: 0,
            categories = book?.categories,
            averageRating = book?.averageRating ?: 0.0,
            ratingsCount = book?.ratingsCount ?: 0,
            rating = rating,
            thumbnail = book?.thumbnail,
            image = book?.image,
            format = format,
            state = state,
            isFavourite = book?.isFavourite ?: false
        )
    }

    private fun setEdition(editable: Boolean) {

        val backgroundTint =
            if(editable) {
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
            } else {
                ColorStateList.valueOf(Color.TRANSPARENT)
            }

        menu.findItem(R.id.action_edit).isVisible = !editable
        menu.findItem(R.id.action_remove).isVisible = !editable
        menu.findItem(R.id.action_save).isVisible = editable
        menu.findItem(R.id.action_cancel).isVisible = editable

        rbStars.setIsIndicator(!editable)

        if (etSummary.text.toString() == Constants.NO_VALUE) {
            etSummary.text = null
        }
        etSummary.setReadOnly(!editable, if(editable) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL, 0)
        etSummary.backgroundTintList = backgroundTint
        etSummary.maxLines = if(editable) Constants.MAX_LINES else Constants.MIN_LINES

        spFormats.backgroundTintList = backgroundTint
        spFormats.isEnabled = editable

        spStates.backgroundTintList = backgroundTint
        spStates.isEnabled = editable

        if (etReadingDate.text.toString() == Constants.NO_VALUE) {
            etReadingDate.text = null
        }
        etReadingDate.isEnabled = editable
        etReadingDate.backgroundTintList = backgroundTint
    }
}