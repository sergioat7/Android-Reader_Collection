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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.extensions.getValue
import aragones.sergio.readercollection.extensions.setReadOnly
import aragones.sergio.readercollection.extensions.showDatePicker
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_book_detail.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar
import java.util.*
import kotlin.math.abs

class BookDetailFragment : BaseFragment(), AppBarLayout.OnOffsetChangedListener {

    //MARK: - Private properties

    private var bookId: String = ""
    private var isGoogleBook: Boolean = false
    private lateinit var ablBook: AppBarLayout
    private lateinit var clImageToolbar: ConstraintLayout
    private lateinit var ivBook: ImageView
    private lateinit var pbLoadingImage: ProgressBar
    private lateinit var fbAddPhoto: FloatingActionButton
    private lateinit var fbFavourite: FloatingActionButton
    private lateinit var pbLoadingFavourite: ProgressBar
    private lateinit var llRating: LinearLayout
    private lateinit var rbStars: MaterialRatingBar
    private lateinit var tvRatingCount: TextView
    private lateinit var tvNoRatings: TextView
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var svCategories: HorizontalScrollView
    private lateinit var llCategories: LinearLayout
    private lateinit var etDescription: EditText
    private lateinit var btReadMoreDescription: Button
    private lateinit var tvDescriptionCount: TextView
    private lateinit var llSummary: LinearLayout
    private lateinit var etSummary: EditText
    private lateinit var btReadMoreSummary: Button
    private lateinit var tvSummaryCount: TextView
    private lateinit var llTitles1: LinearLayout
    private lateinit var llValues1: LinearLayout
    private lateinit var pbLoadingFormats: ProgressBar
    private lateinit var spFormats: Spinner
    private lateinit var pbLoadingStates: ProgressBar
    private lateinit var spStates: Spinner
    private lateinit var etIsbn: EditText
    private lateinit var etPageCount: EditText
    private lateinit var etPublisher: EditText
    private lateinit var etPublishedDate: EditText
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
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        this.menu = menu
        menu.clear()

        val menuRes =
            if (isGoogleBook) R.menu.google_book_detail_toolbar_menu else R.menu.book_detail_toolbar_menu
        inflater.inflate(menuRes, menu)
        menu.findItem(R.id.action_save).isVisible = isGoogleBook
        if (!isGoogleBook) {
            menu.findItem(R.id.action_cancel).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_save -> {
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    //MARK: - Interface methods

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val maxScroll = appBarLayout?.totalScrollRange ?: 0
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()

        for (view in arrayOf(clImageToolbar, fbAddPhoto, fbFavourite, pbLoadingFavourite)) {
            view.scaleX = 1 - percentage
            view.scaleY = 1 - percentage
        }
    }

    //MARK: - Private methods

    private fun initializeUI() {

        ablBook = app_bar_layout_book_detail
        clImageToolbar = constraint_layout_image_toolbar
        ivBook = image_view_book
        pbLoadingImage = progress_bar_loading_image
        fbAddPhoto = floating_action_button_add_photo
        fbFavourite = floating_action_button_favourite
        pbLoadingFavourite = progress_bar_loading_favourite
        llRating = linear_layout_rating
        rbStars = rating_bar
        tvRatingCount = text_view_rating_count
        tvNoRatings = text_view_no_ratings
        etTitle = edit_text_title
        etAuthor = edit_text_author
        svCategories = horizontal_scroll_view_categories
        llCategories = linear_layout_categories
        etDescription = edit_text_description
        btReadMoreDescription = button_read_more_description
        tvDescriptionCount = text_view_description_count
        llSummary = linear_layout_summary
        etSummary = edit_text_summary
        btReadMoreSummary = button_read_more_summary
        tvSummaryCount = text_view_summary_count
        llTitles1 = linear_layout_titles_1
        llValues1 = linear_layout_values_1
        pbLoadingFormats = progress_bar_loading_formats
        spFormats = spinner_formats
        pbLoadingStates = progress_bar_loading_states
        spStates = spinner_states
        etIsbn = edit_text_isbn
        etPageCount = edit_text_page_count
        etPublisher = edit_text_publisher
        etPublishedDate = edit_text_published_date
        llTitles4 = linear_layout_titles_4
        llValues4 = linear_layout_values_4
        etReadingDate = edit_text_reading_date
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            BookDetailViewModelFactory(application, bookId, isGoogleBook)
        )[BookDetailViewModel::class.java]
        setupBindings()
        formats = listOf()
        formatValues = mutableListOf()
        states = listOf()
        stateValues = mutableListOf()

        ablBook.addOnOffsetChangedListener(this)

        val screenSize = Constants.getScreenSize(requireContext() as BookDetailActivity)
        clImageToolbar.layoutParams = CollapsingToolbarLayout.LayoutParams(
            CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
            (screenSize.second * 0.5).toInt(),
            Gravity.CENTER
        )

        fbAddPhoto.setOnClickListener {
            //TODO: implement action
        }

        fbFavourite.visibility = if (isGoogleBook) View.GONE else View.VISIBLE
        pbLoadingFavourite.visibility = View.GONE
        fbFavourite.setOnClickListener {
            viewModel.setFavourite(!isFavourite)
        }

        rbStars.setIsIndicator(true)

        etTitle.setReadOnly(true, InputType.TYPE_NULL, 0)

        etAuthor.setReadOnly(true, InputType.TYPE_NULL, 0)

        etDescription.setReadOnly(true, InputType.TYPE_NULL, 0)
        etDescription.doAfterTextChanged {
            tvDescriptionCount.text = resources.getString(R.string.book_text_count, it?.length)
        }

        btReadMoreDescription.setOnClickListener {

            etDescription.maxLines = Constants.MAX_LINES
            btReadMoreDescription.visibility = View.GONE
        }

        etSummary.setReadOnly(true, InputType.TYPE_NULL, 0)
        etSummary.doAfterTextChanged {
            tvSummaryCount.text = resources.getString(R.string.book_text_count, it?.length)
        }

        btReadMoreSummary.setOnClickListener {

            etSummary.maxLines = Constants.MAX_LINES
            btReadMoreSummary.visibility = View.GONE
        }

        spFormats.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        spFormats.isEnabled = false

        spStates.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        spStates.isEnabled = false

        etIsbn.setReadOnly(true, InputType.TYPE_NULL, 0)

        etPageCount.setReadOnly(true, InputType.TYPE_NULL, 0)

        etPublisher.setReadOnly(true, InputType.TYPE_NULL, 0)

        etPublishedDate.setOnClickListener {
            etPublishedDate.showDatePicker(requireActivity())
        }
        etPublishedDate.isEnabled = false
        etPublishedDate.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

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
            makeFieldsEditable(isGoogleBook || it == null)
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
            pbLoadingFormats.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookDetailStatesLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingStates.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookDetailFavouriteLoading.observe(viewLifecycleOwner, { isLoading ->

            fbFavourite.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            pbLoadingFavourite.visibility = if (isLoading) View.VISIBLE else View.GONE
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
            book.thumbnail?.replace("http", "https") ?: book.image?.replace("http", "https") ?: "-"
        Picasso
            .get()
            .load(image)
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

        val title = book.title ?: ""
        etTitle.setText(
            if (title.isNotBlank()) title
            else Constants.NO_VALUE
        )

        val authors = book.authors?.joinToString(separator = ", ") ?: ""
        etAuthor.setText(
            if (authors.isNotBlank()) resources.getString(R.string.authors_text, authors)
            else Constants.NO_VALUE
        )

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
        etDescription.setText(description)

        tvDescriptionCount.text = resources.getString(R.string.book_text_count, description.length)

        btReadMoreDescription.visibility =
            if (description == Constants.NO_VALUE || etDescription.maxLines == Constants.MAX_LINES) {
                View.GONE
            } else {
                View.VISIBLE
            }

        var summary = book.summary
        if (summary == null || summary.isBlank()) {
            summary = Constants.NO_VALUE
        }
        etSummary.setText(summary)

        tvSummaryCount.text = resources.getString(R.string.book_text_count, summary.length)

        btReadMoreSummary.visibility =
            if (summary == Constants.NO_VALUE || etSummary.maxLines == Constants.MAX_LINES) {
                View.GONE
            } else {
                View.VISIBLE
            }

        setFormat(book)

        setState(book)

        var isbn = Constants.NO_VALUE
        if (book.isbn != null && book.isbn.isNotBlank()) {
            isbn = book.isbn
        }
        etIsbn.setText(isbn)

        etPageCount.setText(book.pageCount.toString())

        var publisher = Constants.NO_VALUE
        if (book.publisher != null && book.publisher.isNotBlank()) {
            publisher = book.publisher
        }
        etPublisher.setText(publisher)

        var publishedDate = Constants.dateToString(
            book.publishedDate,
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        if (publishedDate == null || publishedDate.isBlank()) {
            publishedDate = Constants.NO_VALUE
        }
        etPublishedDate.setText(publishedDate)

        var readingDate = Constants.dateToString(
            book.readingDate,
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        if (readingDate == null || readingDate.isBlank()) {
            readingDate = Constants.NO_VALUE
        }
        etReadingDate.setText(readingDate)
    }

    private fun setFormat(book: BookResponse) {

        var formatPosition = 0
        book.format?.let { formatId ->

            val formatName = formats.firstOrNull { it.id == formatId }?.name
            val pos = formatValues.indexOf(formatName)
            formatPosition = if (pos > 0) pos else 0
        }
        spFormats.setSelection(formatPosition)
    }

    private fun setState(book: BookResponse) {

        var statePosition = if (isGoogleBook) 1 else 0
        book.state?.let { stateId ->

            val stateName = states.firstOrNull { it.id == stateId }?.name
            val pos = stateValues.indexOf(stateName)
            statePosition = if (pos > 0) pos else 0
        }
        spStates.setSelection(statePosition)
    }

    private fun getBookData(): BookResponse {

        val prefix = resources.getString(R.string.authors_text).split(" ")[0]
        val authorsValue = etAuthor.getValue().removePrefix(prefix).trimStart().trimEnd()
        val authors = Constants.stringToList<String>(authorsValue).map {
            it.trimStart().trimEnd()
        }
        val publishedDate = Constants.stringToDate(
            etPublishedDate.text.toString(),
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        var readingDate = Constants.stringToDate(
            etReadingDate.text.toString(),
            Constants.getDateFormatToShow(viewModel.sharedPreferencesHandler),
            viewModel.sharedPreferencesHandler.getLanguage()
        )
        val pageCountText = etPageCount.getValue()
        val pageCount =
            if (pageCountText.isNotBlank()) pageCountText.toInt()
            else 0
        val rating = rbStars.rating.toDouble() * 2
        val format =
            viewModel.formats.value?.firstOrNull { it.name == spFormats.selectedItem.toString() }?.id
        val state =
            viewModel.states.value?.firstOrNull { it.name == spStates.selectedItem.toString() }?.id
        if (readingDate == null && state == Constants.READ_STATE) readingDate = Date()

        return BookResponse(
            id = book?.id ?: "",
            title = etTitle.getValue(),
            subtitle = book?.subtitle,
            authors = authors,
            publisher = etPublisher.getValue(),
            publishedDate = publishedDate,
            readingDate = readingDate,
            description = etDescription.getValue(),
            summary = etSummary.getValue(),
            isbn = etIsbn.getValue(),
            pageCount = pageCount,
            categories = book?.categories,
            averageRating = book?.averageRating ?: 0.0,
            ratingsCount = book?.ratingsCount ?: 0,
            rating = rating,
            thumbnail = book?.thumbnail,
            image = book?.image,
            format = format,
            state = state,
            isFavourite = isFavourite
        )
    }

    private fun setEdition(editable: Boolean) {

        menu.apply {
            findItem(R.id.action_edit).isVisible = !editable
            findItem(R.id.action_remove).isVisible = !editable
            findItem(R.id.action_save).isVisible = editable
            findItem(R.id.action_cancel).isVisible = editable
        }

        makeFieldsEditable(editable)
    }

    private fun setEditTextEdition(
        editText: EditText,
        editable: Boolean,
        inputType: Int,
        backgroundTint: ColorStateList?
    ) {

        if (editText.text.toString() == Constants.NO_VALUE) {
            editText.text = null
        }
        editText.setReadOnly(!editable, if (editable) inputType else InputType.TYPE_NULL, 0)
        editText.backgroundTintList = backgroundTint
    }

    private fun makeFieldsEditable(editable: Boolean) {

        val backgroundTint =
            if (editable) {
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.colorPrimary
                    )
                )
            } else {
                ColorStateList.valueOf(Color.TRANSPARENT)
            }

        rbStars.setIsIndicator(!editable)

        setEditTextEdition(
            etTitle,
            editable,
            InputType.TYPE_CLASS_TEXT,
            backgroundTint
        )

        setEditTextEdition(
            etAuthor,
            editable,
            InputType.TYPE_CLASS_TEXT,
            backgroundTint
        )

        setEditTextEdition(
            etDescription,
            editable,
            InputType.TYPE_CLASS_TEXT,
            backgroundTint
        )

        setEditTextEdition(
            etSummary,
            editable,
            InputType.TYPE_CLASS_TEXT,
            backgroundTint
        )

        spFormats.backgroundTintList = backgroundTint
        spFormats.isEnabled = editable

        spStates.backgroundTintList = backgroundTint
        spStates.isEnabled = editable

        setEditTextEdition(
            etIsbn,
            editable,
            InputType.TYPE_CLASS_NUMBER,
            backgroundTint
        )

        setEditTextEdition(
            etPageCount,
            editable,
            InputType.TYPE_CLASS_NUMBER,
            backgroundTint
        )

        setEditTextEdition(
            etPublisher,
            editable,
            InputType.TYPE_CLASS_TEXT,
            backgroundTint
        )

        if (etPublishedDate.text.toString() == Constants.NO_VALUE) {
            etPublishedDate.text = null
        }
        etPublishedDate.isEnabled = editable
        etPublishedDate.backgroundTintList = backgroundTint

        if (etReadingDate.text.toString() == Constants.NO_VALUE) {
            etReadingDate.text = null
        }
        etReadingDate.isEnabled = editable
        etReadingDate.backgroundTintList = backgroundTint
    }
}