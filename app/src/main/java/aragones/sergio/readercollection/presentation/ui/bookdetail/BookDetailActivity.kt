/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.bookdetail

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.databinding.ActivityBookDetailBinding
import aragones.sergio.readercollection.databinding.CustomTextInputLayoutBinding
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.extensions.addChip
import aragones.sergio.readercollection.presentation.extensions.doAfterTextChanged
import aragones.sergio.readercollection.presentation.extensions.getScreenSize
import aragones.sergio.readercollection.presentation.extensions.getSpannableFor
import aragones.sergio.readercollection.presentation.extensions.getValue
import aragones.sergio.readercollection.presentation.extensions.isBlank
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.extensions.setEndIconOnClickListener
import aragones.sergio.readercollection.presentation.extensions.setHintStyle
import aragones.sergio.readercollection.presentation.extensions.setNavigationBarColorStyle
import aragones.sergio.readercollection.presentation.extensions.setOnClickListener
import aragones.sergio.readercollection.presentation.extensions.setStatusBarStyle
import aragones.sergio.readercollection.presentation.extensions.setValue
import aragones.sergio.readercollection.presentation.extensions.showDatePicker
import aragones.sergio.readercollection.presentation.extensions.style
import aragones.sergio.readercollection.presentation.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.presentation.ui.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.TextFieldAlertDialog
import aragones.sergio.readercollection.presentation.ui.modals.loading.PopupLoadingDialogFragment
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.utils.Constants.FORMATS
import aragones.sergio.readercollection.utils.Constants.STATES
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.CustomDropdownType
import com.aragones.sergio.util.extensions.isNotBlank
import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toList
import com.aragones.sergio.util.extensions.toString
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookDetailActivity :
    AppCompatActivity(),
    MenuProviderInterface,
    AppBarLayout.OnOffsetChangedListener {

    //region Private properties
    private val viewModel: BookDetailViewModel by viewModels()
    private lateinit var binding: ActivityBookDetailBinding
    private lateinit var toolbar: Toolbar
    private var loadingFragment: PopupLoadingDialogFragment? = null
    private var book: Book? = null
    private lateinit var menu: Menu
    private lateinit var mainContentSequence: TapTargetSequence
    private var newBookToolbarSequence: TapTargetSequence? = null
    private var bookDetailsToolbarSequence: TapTargetSequence? = null
    private var mainContentSequenceShown = false
    private var isStyleBeingApplied = false
    private val menuProviderInterface = this
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setStatusBarStyle(
            ContextCompat.getColor(this, R.color.colorPrimary),
            isDarkMode(),
        )
        window.setNavigationBarColorStyle(
            ContextCompat.getColor(this, R.color.colorSecondary),
            !isDarkMode(),
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_detail)
        binding.also {
            it.root.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorSecondary,
                ),
            )
        }
        setContentView(binding.root)
        initializeUi()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            binding.container.setPadding(0)
        }

        binding.composeView.setContent {
            ReaderCollectionTheme {
                val confirmationMessageId by viewModel.confirmationDialogMessageId.observeAsState(
                    initial = -1,
                )
                val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(
                    initial = -1,
                )
                val imageDialogMessageId by viewModel.imageDialogMessageId.observeAsState(
                    initial = -1,
                )

                ConfirmationAlertDialog(
                    show = confirmationMessageId != -1,
                    textId = confirmationMessageId,
                    onCancel = {
                        viewModel.closeDialogs()
                    },
                    onAccept = {
                        viewModel.closeDialogs()
                        viewModel.deleteBook()
                    },
                )

                val text = if (infoDialogMessageId != -1) {
                    getString(infoDialogMessageId)
                } else {
                    ""
                }
                InformationAlertDialog(show = infoDialogMessageId != -1, text = text) {
                    viewModel.closeDialogs()
                    finish()
                }

                TextFieldAlertDialog(
                    show = imageDialogMessageId != -1,
                    titleTextId = imageDialogMessageId,
                    type = KeyboardType.Uri,
                    onCancel = {
                        viewModel.closeDialogs()
                    },
                    onAccept = {
                        viewModel.closeDialogs()
                        if (it.isNotBlank()) viewModel.setBookImage(it)
                    },
                )
            }
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuProviderInterface.onCreateMenu(menu, menuInflater)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    menuProviderInterface.onMenuItemSelected(menuItem)
            },
            this,
        )

        onBackPressedDispatcher.addCallback {
            finish()
        }

        viewModel.onCreate()
    }

    override fun onStart() {
        super.onStart()

        createSequence()
    }

    override fun onStop() {
        super.onStop()

        if (!viewModel.newBookTutorialShown && viewModel.isGoogleBook) {
            mainContentSequence.cancel()
            newBookToolbarSequence?.cancel()
        }
        if (!viewModel.bookDetailsTutorialShown && !viewModel.isGoogleBook) {
            bookDetailsToolbarSequence?.cancel()
        }
    }
    //endregion

    //region Interface methods
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        this.menu = menu
        menu.clear()

        val menuRes =
            if (viewModel.isGoogleBook) {
                R.menu.google_book_detail_toolbar_menu
            } else {
                R.menu.book_detail_toolbar_menu
            }
        menuInflater.inflate(menuRes, menu)
        menu.findItem(R.id.action_save).isVisible = viewModel.isGoogleBook
        if (!viewModel.isGoogleBook) {
            menu.findItem(R.id.action_cancel).isVisible = false
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_save -> {
                if (viewModel.isGoogleBook) {
                    viewModel.createBook(getBookData())
                } else {
                    viewModel.setBook(getBookData())
                    setEdition(false)
                }
            }
            R.id.action_edit -> {
                setEdition(true)
            }
            R.id.action_remove -> {
                viewModel.showConfirmationDialog(
                    R.string.book_remove_confirmation,
                )
            }
            R.id.action_cancel -> {
                setEdition(false)
                book?.let { showData(it) }
            }
        }
        return false
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val maxScroll = appBarLayout?.totalScrollRange ?: 0
        val percentage = abs(verticalOffset).toFloat() / maxScroll.toFloat()

        with(binding) {
            for (view in arrayOf(
                constraintLayoutImageToolbar,
                floatingActionButtonAddPhoto,
                floatingActionButtonFavourite,
                progressBarLoadingFavourite,
            )) {
                view.scaleX = 1 - percentage
                view.scaleY = 1 - percentage
                view.isEnabled = percentage < 0.6
            }
        }
    }
    //endregion

    //region Public methods
    fun setImage() {
        viewModel.showImageDialog(R.string.enter_valid_url)
    }

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
    private fun initializeUi() {
        toolbar = binding.toolbar.also {
            setSupportActionBar(it)
            it.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        setupBindings()

        val language = viewModel.language
        with(binding) {
            appBarLayoutBookDetail.addOnOffsetChangedListener(this@BookDetailActivity)

            val screenSize = getScreenSize()
            constraintLayoutImageToolbar.layoutParams = CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                (screenSize.second * 0.5).toInt(),
                Gravity.CENTER,
            )

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
                textInputLayoutReadingDate,
            )) {
                view.setHintStyle(R.style.Widget_ReaderCollection_TextView_Header)
                view.setEndIconOnClickListener {
                    view.textInputEditText.setText("")
                }
            }

            textInputLayoutDescription.doAfterTextChanged {
                buttonReadMoreDescription.visibility =
                    if (textInputLayoutDescription.isBlank() ||
                        textInputLayoutDescription.textInputEditText.lineCount < 8 ||
                        textInputLayoutDescription.maxLines == Constants.MAX_LINES
                    ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                applyStyleTo(textInputLayoutDescription)
            }
            applyStyleTo(textInputLayoutDescription)

            textInputLayoutSummary.doAfterTextChanged {
                buttonReadMoreSummary.visibility =
                    if (textInputLayoutSummary.isBlank() ||
                        textInputLayoutSummary.textInputEditText.lineCount < 8 ||
                        textInputLayoutSummary.maxLines == Constants.MAX_LINES
                    ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                applyStyleTo(textInputLayoutSummary)
            }
            applyStyleTo(textInputLayoutSummary)

            textInputLayoutReadingDate.setOnClickListener {
                textInputLayoutReadingDate.showDatePicker(this@BookDetailActivity, language)
            }

            dropdownTextInputLayoutFormat.setHintStyle(
                R.style.Widget_ReaderCollection_TextView_Header,
            )

            dropdownTextInputLayoutState.setHintStyle(
                R.style.Widget_ReaderCollection_TextView_Header,
            )

            textInputLayoutPublishedDate.setOnClickListener {
                textInputLayoutPublishedDate.showDatePicker(this@BookDetailActivity, language)
            }

            activity = this@BookDetailActivity
            viewModel = this@BookDetailActivity.viewModel
            lifecycleOwner = this@BookDetailActivity
            editable = false
            isDarkMode = isDarkMode()
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {
        viewModel.book.observe(this) {
            book = it
            showData(it)
            binding.editable = viewModel.isGoogleBook || it == null
        }

        viewModel.bookDetailLoading.observe(this) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.bookDetailError.observe(this) {
            book?.let { b -> showData(b) }
            it?.let { manageError(it) }
        }
    }

    private fun showData(book: Book) {
        binding.chipGroupCategories.removeAllViews()
        book.categories?.let { categories ->
            for (category in categories) {
                binding.chipGroupCategories.addChip(layoutInflater, category)
            }
        }

        binding.textInputLayoutReadingDate.text = getTextDate(viewModel.book.value?.readingDate)

        setFormat(book)
        setState(book)

        binding.textInputLayoutPublishedDate.text = getTextDate(viewModel.book.value?.publishedDate)

        viewModel.setBookImage(book.thumbnail ?: book.image)
    }

    private fun setFormat(book: Book) {
        /*
         * This works before it's executed AFTER onResume method.
         * Otherwise, we must be sure to place it in onResume method.
         */
        binding.dropdownTextInputLayoutFormat.setValue(
            book.format,
            CustomDropdownType.FORMAT,
        )
    }

    private fun setState(book: Book) {
        /*
         * This works before it's executed AFTER onResume method.
         * Otherwise, we must be sure to place it in onResume method.
         */
        binding.dropdownTextInputLayoutState.setValue(
            book.state ?: STATES.first().id,
            CustomDropdownType.STATE,
        )
    }

    private fun getTextDate(date: Date?): String? {
        val language = viewModel.language
        val dateFormatToShow = Constants.getDateFormatToShow(language)
        return date?.toString(dateFormatToShow, language)
            ?: if (binding.editable == true) null else Constants.NO_VALUE
    }

    private fun getBookData(): Book {
        val language = viewModel.language
        val dateFormatToShow = Constants.getDateFormatToShow(language)
        with(binding) {
            val authors = textInputLayoutAuthor.getValue().toList<String>().map {
                it.trimStart().trimEnd()
            }
            val publishedDate = textInputLayoutPublishedDate.getValue().toDate(
                dateFormatToShow,
                language,
            )
            var readingDate = textInputLayoutReadingDate.getValue().toDate(
                dateFormatToShow,
                language,
            )
            val pageCountText = textInputLayoutPages.getValue()
            val pageCount =
                if (pageCountText.isNotBlank()) {
                    pageCountText.toInt()
                } else {
                    0
                }
            val rating = ratingBar.rating.toDouble() * 2
            val format =
                FORMATS.firstOrNull { it.name == dropdownTextInputLayoutFormat.getValue() }?.id
            var state =
                STATES.firstOrNull { it.name == dropdownTextInputLayoutState.getValue() }?.id
            if (state != BookState.READ && book?.readingDate == null && readingDate != null) {
                state = BookState.READ
            }
            if (book?.readingDate == null && readingDate == null && state == BookState.READ) {
                readingDate = Date()
            }
            val isFavourite = this@BookDetailActivity.viewModel.isFavourite.value ?: false

            return Book(
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
                thumbnail = this@BookDetailActivity.viewModel.bookImage.value,
                image = book?.image,
                format = format,
                state = state,
                isFavourite = isFavourite,
                priority = book?.priority ?: -1,
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
        binding.textInputLayoutReadingDate.text = getTextDate(viewModel.book.value?.readingDate)
        binding.textInputLayoutPublishedDate.text = getTextDate(viewModel.book.value?.publishedDate)
    }

    private fun manageError(
        errorResponse: ErrorResponse,
        goBack: MutableLiveData<Boolean>? = null,
    ) {
        val error = StringBuilder()
        if (errorResponse.error.isNotEmpty()) {
            error.append(errorResponse.error)
        } else {
            error.append(resources.getString(errorResponse.errorKey))
        }
        showPopupDialog(error.toString(), goBack)
    }

    private fun showLoading() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("loadingDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        loadingFragment = PopupLoadingDialogFragment()
        loadingFragment?.let {
            it.isCancelable = false
            it.show(ft, "loadingDialog")
        }
    }

    private fun hideLoading() {
        loadingFragment?.dismiss()
        loadingFragment = null
    }

    private fun createTargetsForNewBookToolbar(): List<TapTarget> {
        val saveBookItem = binding.toolbar.menu.findItem(R.id.action_save)
        return listOf(
            TapTarget
                .forToolbarMenuItem(
                    binding.toolbar,
                    saveBookItem.itemId,
                    resources.getString(R.string.add_book_icon_tutorial_title),
                    resources.getString(R.string.add_book_icon_tutorial_description),
                ).style(this, true)
                .cancelable(true)
                .tintTarget(true),
        )
    }

    private fun createTargetsForScrollView(): List<TapTarget> = listOf(
        TapTarget
            .forView(
                binding.floatingActionButtonAddPhoto,
                resources.getString(R.string.add_image_button_tutorial_title),
                resources.getString(R.string.add_image_button_tutorial_description),
            ).style(this, true)
            .cancelable(true)
            .tintTarget(false),
        TapTarget
            .forView(
                binding.ratingBar,
                resources.getString(R.string.rate_view_tutorial_title),
                resources.getString(R.string.rate_view_tutorial_description),
            ).style(this)
            .cancelable(true)
            .tintTarget(true),
    )

    private fun createTargetsForBookDetailsToolbar(): List<TapTarget> {
        val editBookItem = binding.toolbar.menu.findItem(R.id.action_edit)
        val deleteBookItem = binding.toolbar.menu.findItem(R.id.action_remove)
        return listOf(
            TapTarget
                .forToolbarMenuItem(
                    binding.toolbar,
                    editBookItem.itemId,
                    resources.getString(R.string.edit_book_icon_tutorial_title),
                    resources.getString(R.string.edit_book_icon_tutorial_description),
                ).style(this, true)
                .cancelable(true)
                .tintTarget(true),
            TapTarget
                .forToolbarMenuItem(
                    binding.toolbar,
                    deleteBookItem.itemId,
                    resources.getString(R.string.delete_book_icon_tutorial_title),
                    resources.getString(R.string.delete_book_icon_tutorial_description),
                ).style(this, true)
                .cancelable(true)
                .tintTarget(true),
        )
    }

    private fun createSequence() {
        if (!viewModel.newBookTutorialShown && viewModel.isGoogleBook) {
            mainContentSequence = TapTargetSequence(this).apply {
                targets(createTargetsForScrollView())
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        mainContentSequenceShown = true
                        newBookToolbarSequence?.start()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                if (!mainContentSequenceShown) {
                    start()
                }
            }
        }
        /*
        Must be created with a delay in order to wait for the fragment menu creation,
        otherwise it wouldn't be icons in the toolbar
         */
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)

            if (!viewModel.newBookTutorialShown && viewModel.isGoogleBook) {
                newBookToolbarSequence = TapTargetSequence(this@BookDetailActivity).apply {
                    targets(createTargetsForNewBookToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setNewBookTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean,
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    if (mainContentSequenceShown) {
                        start()
                    }
                }
            } else if (!viewModel.bookDetailsTutorialShown && !viewModel.isGoogleBook) {
                bookDetailsToolbarSequence = TapTargetSequence(this@BookDetailActivity).apply {
                    targets(createTargetsForBookDetailsToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setBookDetailsTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean,
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    start()
                }
            }
        }
    }

    private fun applyStyleTo(input: CustomTextInputLayoutBinding) {
        if (!isStyleBeingApplied && binding.editable == false) {
            val selection = input.textInputEditText.selectionEnd
            val spannable = input.getSpannableFor(Typeface.BOLD)
            isStyleBeingApplied = true
            input.textInputEditText.text = spannable
            input.textInputEditText.setSelection(selection)
            isStyleBeingApplied = false
        }
    }

    private fun showPopupDialog(message: String, goBack: MutableLiveData<Boolean>? = null) {
        MaterialAlertDialogBuilder(
            this,
            R.style.ThemeOverlay_ReaderCollection_MaterialAlertDialog,
        ).setMessage(message)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->

                dialog.dismiss()
                goBack?.let {
                    it.value = true
                }
            }.show()
    }
    //endregion
}