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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentBookDetailBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.utils.State
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_book_detail.*
import java.util.*
import kotlin.math.abs

class BookDetailFragment : BindingFragment<FragmentBookDetailBinding>(),
    AppBarLayout.OnOffsetChangedListener {

    //region Protected properties
    override val hasOptionsMenu = true
    //endregion

    //region Private properties
    private var bookId: String = ""
    private lateinit var viewModel: BookDetailViewModel
    private var book: BookResponse? = null
    private lateinit var formats: List<FormatResponse>
    private lateinit var formatValues: MutableList<String>
    private lateinit var states: List<StateResponse>
    private lateinit var stateValues: MutableList<String>
    private val goBack = MutableLiveData<Boolean>()
    private lateinit var menu: Menu
    //endregion

    //region Lifecycle methods
    companion object {
        fun newInstance() = BookDetailFragment()
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
            if (viewModel.isGoogleBook) R.menu.google_book_detail_toolbar_menu
            else R.menu.book_detail_toolbar_menu
        inflater.inflate(menuRes, menu)
        menu.findItem(R.id.action_save).isVisible = viewModel.isGoogleBook
        if (!viewModel.isGoogleBook) {
            menu.findItem(R.id.action_cancel).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_save -> {
                if (viewModel.isGoogleBook) {
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

        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val maxScroll = appBarLayout?.totalScrollRange ?: 0
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()

        with(binding) {
            for (view in arrayOf(
                constraintLayoutImageToolbar,
                floatingActionButtonAddPhoto,
                floatingActionButtonFavourite,
                progressBarLoadingFavourite
            )) {
                view.scaleX = 1 - percentage
                view.scaleY = 1 - percentage
            }
        }
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        bookId = this.arguments?.getString(Constants.BOOK_ID) ?: ""
        val isGoogleBook = this.arguments?.getBoolean(Constants.IS_GOOGLE_BOOK) ?: false

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

        with(binding) {
            appBarLayoutBookDetail.addOnOffsetChangedListener(this@BookDetailFragment)

            val screenSize = requireActivity().getScreenSize()
            constraintLayoutImageToolbar.layoutParams = CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                (screenSize.second * 0.5).toInt(),
                Gravity.CENTER
            )

            floatingActionButtonAddPhoto.setOnClickListener {
                //TODO: implement action
            }

            editTextTitle.setReadOnly(true, InputType.TYPE_NULL, 0)

            editTextAuthor.setReadOnly(true, InputType.TYPE_NULL, 0)

            editTextDescription.setReadOnly(true, InputType.TYPE_NULL, 0)
            editTextDescription.doAfterTextChanged {
                textViewDescriptionCount.text =
                    resources.getString(R.string.book_text_count, it?.length)
            }

            buttonReadMoreDescription.setOnClickListener {

                editTextDescription.maxLines = Constants.MAX_LINES
                buttonReadMoreDescription.visibility = View.GONE
            }

            editTextSummary.setReadOnly(true, InputType.TYPE_NULL, 0)
            editTextSummary.doAfterTextChanged {
                textViewSummaryCount.text =
                    resources.getString(R.string.book_text_count, it?.length)
            }

            buttonReadMoreSummary.setOnClickListener {

                editTextSummary.maxLines = Constants.MAX_LINES
                buttonReadMoreSummary.visibility = View.GONE
            }

            spinnerFormats.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            spinnerFormats.isEnabled = false

            spinnerStates.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            spinnerStates.isEnabled = false

            editTextIsbn.setReadOnly(true, InputType.TYPE_NULL, 0)

            editTextPageCount.setReadOnly(true, InputType.TYPE_NULL, 0)

            editTextPublisher.setReadOnly(true, InputType.TYPE_NULL, 0)

            editTextPublishedDate.setOnClickListener {
                editTextPublishedDate.showDatePicker(requireActivity())
            }
            editTextPublishedDate.isEnabled = false
            editTextPublishedDate.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

            editTextReadingDate.setOnClickListener {
                editTextReadingDate.showDatePicker(requireActivity())
            }
            editTextReadingDate.isEnabled = false
            editTextReadingDate.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

            fragment = this@BookDetailFragment
            viewModel = this@BookDetailFragment.viewModel
            lifecycleOwner = this@BookDetailFragment
            editable = false
        }
    }

    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner, {

            book = it
            showData(it)
            makeFieldsEditable(viewModel.isGoogleBook || it == null)
        })

        viewModel.formats.observe(viewLifecycleOwner, { formatsResponse ->

            formats = formatsResponse
            formatValues = mutableListOf()
            formatValues.run {

                this.add(resources.getString((R.string.select_format)))
                this.addAll(formatsResponse.map { it.name })
            }
            binding.spinnerFormats.setup(formatValues, 0)
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
            binding.spinnerStates.setup(stateValues, 0)
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
        with(binding) {
            this.book = book

            val image =
                book.thumbnail?.replace("http", "https") ?: book.image?.replace("http", "https")
                ?: "-"
            Picasso
                .get()
                .load(image)
                .error(R.drawable.ic_default_book_cover)
                .into(imageViewBook, object : Callback {

                    override fun onSuccess() {
                        progressBarLoadingImage.visibility = View.GONE
                    }

                    override fun onError(e: Exception) {
                        progressBarLoadingImage.visibility = View.GONE
                    }
                })

            val rating = if (this@BookDetailFragment.viewModel.isGoogleBook) book.averageRating else book.rating
            ratingBar.rating = rating.toFloat() / 2

            val authors = book.authors?.joinToString(separator = ", ") ?: ""
            editTextAuthor.setText(
                if (authors.isNotBlank()) resources.getString(R.string.authors_text, authors)
                else Constants.NO_VALUE
            )

            linearLayoutCategories.removeAllViews()
            book.categories?.let { categories ->
                for (category in categories) {

                    val tv = Constants.getRoundedTextView(category, requireContext())
                    linearLayoutCategories.addView(tv)

                    val view = View(context)
                    view.layoutParams = ViewGroup.LayoutParams(
                        20,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    linearLayoutCategories.addView(view)
                }
            }

            buttonReadMoreDescription.visibility =
                if (book.description == null || book.description.isBlank() || editTextDescription.maxLines == Constants.MAX_LINES) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            buttonReadMoreSummary.visibility =
                if (book.summary == null || book.summary.isBlank() || editTextDescription.maxLines == Constants.MAX_LINES) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            setFormat(book)

            setState(book)

            var publishedDate = book.publishedDate.toString(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            if (publishedDate == null || publishedDate.isBlank()) {
                publishedDate = Constants.NO_VALUE
            }
            editTextPublishedDate.setText(publishedDate)

            var readingDate = book.readingDate.toString(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            if (readingDate == null || readingDate.isBlank()) {
                readingDate = Constants.NO_VALUE
            }
            editTextReadingDate.setText(readingDate)
        }
    }

    private fun setFormat(book: BookResponse) {

        var formatPosition = 0
        book.format?.let { formatId ->

            val formatName = formats.firstOrNull { it.id == formatId }?.name
            val pos = formatValues.indexOf(formatName)
            formatPosition = if (pos > 0) pos else 0
        }
        binding.spinnerFormats.setSelection(formatPosition)
    }

    private fun setState(book: BookResponse) {

        var statePosition = if (viewModel.isGoogleBook) 1 else 0
        book.state?.let { stateId ->

            val stateName = states.firstOrNull { it.id == stateId }?.name
            val pos = stateValues.indexOf(stateName)
            statePosition = if (pos > 0) pos else 0
        }
        binding.spinnerStates.setSelection(statePosition)
    }

    private fun getBookData(): BookResponse {
        with(binding) {

            val prefix = resources.getString(R.string.authors_text).split(" ")[0]
            val authorsValue = editTextAuthor.getValue().removePrefix(prefix)
            val authors = authorsValue.toList<String>().map {
                it.trimStart().trimEnd()
            }
            val publishedDate = editTextPublishedDate.text.toString().toDate(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            var readingDate = editTextReadingDate.text.toString().toDate(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            val pageCountText = editTextPageCount.getValue()
            val pageCount =
                if (pageCountText.isNotBlank()) pageCountText.toInt()
                else 0
            val rating = ratingBar.rating.toDouble() * 2
            val format =
                this@BookDetailFragment.viewModel.formats.value?.firstOrNull { it.name == spinnerFormats.selectedItem.toString() }?.id
            val state =
                this@BookDetailFragment.viewModel.states.value?.firstOrNull { it.name == spinnerStates.selectedItem.toString() }?.id
            if (readingDate == null && state == State.READ) readingDate = Date()
            val isFavourite = this@BookDetailFragment.viewModel.isFavourite.value ?: false

            return BookResponse(
                id = book?.id ?: "",
                title = editTextTitle.getValue(),
                subtitle = book?.subtitle,
                authors = authors,
                publisher = editTextPublisher.getValue(),
                publishedDate = publishedDate,
                readingDate = readingDate,
                description = editTextDescription.getValue(),
                summary = editTextSummary.getValue(),
                isbn = editTextIsbn.getValue(),
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

    private fun makeFieldsEditable(editable: Boolean) {
        with(binding) {
            this.editable = editable

            val inputTypeText = if (editable) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
            val inputTypeNumber = if (editable) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_NULL
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

            for (editText in listOf(
                editTextTitle,
                editTextAuthor,
                editTextDescription,
                editTextSummary,
                editTextPublisher
            )) {
                editText.setReadOnly(!editable, inputTypeText, 0)
                editText.backgroundTintList = backgroundTint
            }

            for (editText in listOf(
                editTextIsbn,
                editTextPageCount
            )) {
                editText.setReadOnly(!editable, inputTypeNumber, 0)
                editText.backgroundTintList = backgroundTint
            }

            if (editTextAuthor.text.toString() == Constants.NO_VALUE) {
                editTextAuthor.text = null
            }

            spinnerFormats.backgroundTintList = backgroundTint
            spinnerFormats.isEnabled = editable

            spinnerStates.backgroundTintList = backgroundTint
            spinnerStates.isEnabled = editable

            if (editTextPublishedDate.text.toString() == Constants.NO_VALUE) {
                editTextPublishedDate.text = null
            }
            editTextPublishedDate.backgroundTintList = backgroundTint

            if (editTextReadingDate.text.toString() == Constants.NO_VALUE) {
                editTextReadingDate.text = null
            }
            editTextReadingDate.backgroundTintList = backgroundTint
        }
    }
    //endregion
}