/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.bookdetail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.source.SharedPreferencesHandler
import aragones.sergio.readercollection.databinding.DialogSetImageBinding
import aragones.sergio.readercollection.databinding.FragmentBookDetailBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.utils.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

@AndroidEntryPoint
class BookDetailFragment :
    BindingFragment<FragmentBookDetailBinding>(),
    MenuProviderInterface,
    AppBarLayout.OnOffsetChangedListener {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.SECONDARY
    //endregion

    //region Private properties
    private val viewModel: BookDetailViewModel by viewModels()
    private var book: BookResponse? = null
    private val goBack = MutableLiveData<Boolean>()
    private lateinit var menu: Menu
    private lateinit var mainContentSequence: TapTargetSequence
    private var newBookToolbarSequence: TapTargetSequence? = null
    private var bookDetailsToolbarSequence: TapTargetSequence? = null
    private var mainContentSequenceShown = false
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
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

        viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        this.menu = menu
        menu.clear()

        val menuRes =
            if (viewModel.isGoogleBook) R.menu.google_book_detail_toolbar_menu
            else R.menu.book_detail_toolbar_menu
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
                progressBarLoadingFavourite
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

        val dialogBinding = DialogSetImageBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.enter_valid_url))
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->

                val url = dialogBinding.textInputLayoutImage.getValue()
                if (url.isNotBlank()) viewModel.setBookImage(url)
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
    override fun initializeUi() {
        super.initializeUi()

        setupBindings()

        with(binding) {
            appBarLayoutBookDetail.addOnOffsetChangedListener(this@BookDetailFragment)

            val screenSize = requireActivity().getScreenSize()
            constraintLayoutImageToolbar.layoutParams = CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                (screenSize.second * 0.5).toInt(),
                Gravity.CENTER
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
            isDarkMode = context?.isDarkMode()
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner) {

            book = it
            showData(it)
            binding.editable = viewModel.isGoogleBook || it == null
        }

        viewModel.bookDetailLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.bookDetailSuccessMessage.observe(viewLifecycleOwner) {

            val message = resources.getString(it)
            showPopupDialog(message, goBack)
        }

        viewModel.bookDetailError.observe(viewLifecycleOwner) {

            book?.let { b -> showData(b) }
            it?.let { manageError(it) }
        }

        goBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun showData(book: BookResponse) {

        binding.chipGroupCategories.removeAllViews()
        book.categories?.let { categories ->
            for (category in categories) {
                binding.chipGroupCategories.addChip(layoutInflater, category)
            }
        }

        setFormat(book)
        setState(book)

        viewModel.setBookImage(book.thumbnail ?: book.image)
    }

    private fun setFormat(book: BookResponse) {

        /*
        * This works before it's executed AFTER onResume method.
        * Otherwise, we must be sure to place it in onResume method.
        */
        binding.dropdownTextInputLayoutFormat.setValue(
            book.format,
            CustomDropdownType.FORMAT
        )
    }

    private fun setState(book: BookResponse) {

        /*
        * This works before it's executed AFTER onResume method.
        * Otherwise, we must be sure to place it in onResume method.
        */
        binding.dropdownTextInputLayoutState.setValue(
            book.state ?: Constants.STATES.first().id,
            CustomDropdownType.STATE
        )
    }

    private fun getBookData(): BookResponse {
        with(binding) {

            val authors = textInputLayoutAuthor.getValue().toList<String>().map {
                it.trimStart().trimEnd()
            }
            val publishedDate = textInputLayoutPublishedDate.getValue().toDate(
                SharedPreferencesHandler.dateFormatToShow,
                SharedPreferencesHandler.language
            )
            var readingDate = textInputLayoutReadingDate.getValue().toDate(
                SharedPreferencesHandler.dateFormatToShow,
                SharedPreferencesHandler.language
            )
            val pageCountText = textInputLayoutPages.getValue()
            val pageCount =
                if (pageCountText.isNotBlank()) pageCountText.toInt()
                else 0
            val rating = ratingBar.rating.toDouble() * 2
            val format =
                Constants.FORMATS.firstOrNull { it.name == dropdownTextInputLayoutFormat.getValue() }?.id
            val state =
                Constants.STATES.firstOrNull { it.name == dropdownTextInputLayoutState.getValue() }?.id
            if (book?.readingDate == null && readingDate == null && state == State.READ) readingDate =
                Date()
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
                thumbnail = this@BookDetailFragment.viewModel.bookImage.value,
                image = book?.image,
                format = format,
                state = state,
                isFavourite = isFavourite,
                priority = book?.priority ?: -1
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

    private fun createTargetsForNewBookToolbar(): List<TapTarget> {

        val saveBookItem = binding.toolbar.menu.findItem(R.id.action_save)
        return listOf(
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                saveBookItem.itemId,
                resources.getString(R.string.add_book_icon_tutorial_title),
                resources.getString(R.string.add_book_icon_tutorial_description)
            ).style(requireContext(), true).cancelable(true).tintTarget(true)
        )
    }

    private fun createTargetsForScrollView(): List<TapTarget> {
        return listOf(
            TapTarget.forView(
                binding.floatingActionButtonAddPhoto,
                resources.getString(R.string.add_image_button_tutorial_title),
                resources.getString(R.string.add_image_button_tutorial_description)
            ).style(requireContext(), true).cancelable(true).tintTarget(false),
            TapTarget.forView(
                binding.ratingBar,
                resources.getString(R.string.rate_view_tutorial_title),
                resources.getString(R.string.rate_view_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true)
        )
    }

    private fun createTargetsForBookDetailsToolbar(): List<TapTarget> {

        val editBookItem = binding.toolbar.menu.findItem(R.id.action_edit)
        val deleteBookItem = binding.toolbar.menu.findItem(R.id.action_remove)
        return listOf(
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                editBookItem.itemId,
                resources.getString(R.string.edit_book_icon_tutorial_title),
                resources.getString(R.string.edit_book_icon_tutorial_description)
            ).style(requireContext(), true).cancelable(true).tintTarget(true),
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                deleteBookItem.itemId,
                resources.getString(R.string.delete_book_icon_tutorial_title),
                resources.getString(R.string.delete_book_icon_tutorial_description)
            ).style(requireContext(), true).cancelable(true).tintTarget(true)
        )
    }

    private fun createSequence() {

        if (!viewModel.newBookTutorialShown && viewModel.isGoogleBook) {
            mainContentSequence = TapTargetSequence(requireActivity()).apply {
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
                newBookToolbarSequence = TapTargetSequence(requireActivity()).apply {
                    targets(createTargetsForNewBookToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setNewBookTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    if (mainContentSequenceShown) {
                        start()
                    }
                }
            } else if (!viewModel.bookDetailsTutorialShown && !viewModel.isGoogleBook) {

                bookDetailsToolbarSequence = TapTargetSequence(requireActivity()).apply {
                    targets(createTargetsForBookDetailsToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setBookDetailsTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    start()
                }
            }
        }
    }
    //endregion
}