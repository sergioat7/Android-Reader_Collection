/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentBookDetailBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.utils.State
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import kotlin.math.abs

class BookDetailFragment : BindingFragment<FragmentBookDetailBinding>(),
    AppBarLayout.OnOffsetChangedListener {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.SECONDARY
    //endregion

    //region Private properties
    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var viewModel: BookDetailViewModel
    private var book: BookResponse? = null
    private val goBack = MutableLiveData<Boolean>()
    private lateinit var menu: Menu
    //endregion

    //region Lifecycle methods
    companion object {
        fun newInstance() = BookDetailFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
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

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
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
                view.isEnabled = percentage < 0.75
            }
        }
    }
    //endregion

    //region Public methods
    fun readMore(view: View) {
        with(binding) {

            when (view) {
                buttonReadMoreDescription -> {
                    textInputLayoutDescription.maxLines = Constants.MAX_LINES
                    buttonReadMoreDescription.visibility = View.GONE
                }
                buttonReadMoreSummary -> {
                    textInputLayoutSummary.maxLines = Constants.MAX_LINES
                    buttonReadMoreSummary.visibility = View.GONE
                }
            }
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            BookDetailViewModelFactory(application, args.bookId, args.isGoogleBook)
        )[BookDetailViewModel::class.java]
        setupBindings()

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

            textInputLayoutTitle.setEndIconOnClickListener {
                textInputLayoutTitle.textInputEditText.setText("")
            }
            textInputLayoutAuthor.setEndIconOnClickListener {
                textInputLayoutAuthor.textInputEditText.setText("")
            }

            for (view in listOf(
                textInputLayoutDescription,
                textInputLayoutSummary,
                textInputLayoutIsbn,
                textInputLayoutPages,
                textInputLayoutPublisher,
                textInputLayoutPublishedDate,
                textInputLayoutReadingDate
            )) {
                view.setHintStyle(R.style.Widget_ReaderCollection_TextView_Header)
                view.setEndIconOnClickListener {
                    view.textInputEditText.setText("")
                }
            }

            textInputLayoutDescription.doAfterTextChanged {
                buttonReadMoreDescription.visibility =
                    if (textInputLayoutDescription.isBlank() || textInputLayoutDescription.maxLines == Constants.MAX_LINES) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }

            textInputLayoutSummary.doAfterTextChanged {
                buttonReadMoreSummary.visibility =
                    if (textInputLayoutSummary.isBlank() || textInputLayoutSummary.maxLines == Constants.MAX_LINES) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }

            textInputLayoutPublishedDate.setOnClickListener {
                textInputLayoutPublishedDate.showDatePicker(requireActivity())
            }

            textInputLayoutReadingDate.setOnClickListener {
                textInputLayoutReadingDate.showDatePicker(requireActivity())
            }

            dropdownTextInputLayoutFormat.setHintStyle(R.style.Widget_ReaderCollection_TextView_Header)

            dropdownTextInputLayoutState.setHintStyle(R.style.Widget_ReaderCollection_TextView_Header)

            fragment = this@BookDetailFragment
            viewModel = this@BookDetailFragment.viewModel
            lifecycleOwner = this@BookDetailFragment
            editable = false
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner, {

            book = it
            showData(it)
            binding.editable = viewModel.isGoogleBook || it == null
        })

        viewModel.formats.observe(viewLifecycleOwner, { formatsResponse ->

            binding.dropdownTextInputLayoutFormat.setup(formatsResponse.map { it.name })
            book?.let {
                setFormat(it)
            }
        })

        viewModel.states.observe(viewLifecycleOwner, { statesResponse ->

            binding.dropdownTextInputLayoutState.setup(statesResponse.map { it.name })
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

            setFormat(book)

            setState(book)
        }
    }

    private fun setFormat(book: BookResponse) {

        var currentValue: String? = null
        book.format?.let { formatId ->

            currentValue = viewModel.formats.value?.firstOrNull { it.id == formatId }?.name
        }
        binding.dropdownTextInputLayoutFormat.setValue(currentValue)
    }

    private fun setState(book: BookResponse) {

        var currentValue: String? = viewModel.states.value?.first()?.name
        book.state?.let { stateId ->

            currentValue = viewModel.states.value?.firstOrNull { it.id == stateId }?.name
        }
        binding.dropdownTextInputLayoutState.setValue(currentValue)
    }

    private fun getBookData(): BookResponse {
        with(binding) {

            val authors = textInputLayoutAuthor.getValue().toList<String>().map {
                it.trimStart().trimEnd()
            }
            val publishedDate = textInputLayoutPublishedDate.getValue().toDate(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            var readingDate = textInputLayoutReadingDate.getValue().toDate(
                SharedPreferencesHandler.getDateFormatToShow(),
                SharedPreferencesHandler.getLanguage()
            )
            val pageCountText = textInputLayoutPages.getValue()
            val pageCount =
                if (pageCountText.isNotBlank()) pageCountText.toInt()
                else 0
            val rating = ratingBar.rating.toDouble() * 2
            val format =
                this@BookDetailFragment.viewModel.formats.value?.firstOrNull { it.name == dropdownTextInputLayoutFormat.getValue() }?.id
            val state =
                this@BookDetailFragment.viewModel.states.value?.firstOrNull { it.name == dropdownTextInputLayoutState.getValue() }?.id
            if (readingDate == null && state == State.READ) readingDate = Date()
            val isFavourite = this@BookDetailFragment.viewModel.isFavourite.value ?: false

            return BookResponse(
                id = book?.id ?: "",
                title = textInputLayoutTitle.getValue(),
                subtitle = book?.subtitle,
                authors = authors,
                publisher = textInputLayoutPublisher.getValue(),
                publishedDate = publishedDate,
                readingDate = readingDate,
                description = textInputLayoutDescription.getValue(),
                summary = textInputLayoutSummary.getValue(),
                isbn = textInputLayoutIsbn.getValue(),
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

        binding.editable = editable
    }
    //endregion
}