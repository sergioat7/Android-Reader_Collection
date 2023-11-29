/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.books

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentBooksBinding
import aragones.sergio.readercollection.extensions.hideSoftKeyboard
import aragones.sergio.readercollection.extensions.style
import aragones.sergio.readercollection.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import aragones.sergio.readercollection.utils.StatusBarStyle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BooksFragment :
    BindingFragment<FragmentBooksBinding>(),
    MenuProviderInterface,
    OnItemClickListener,
    OnStartDraggingListener {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: BooksViewModel by viewModels()
    private lateinit var readingBooksAdapter: BooksAdapter
    private lateinit var pendingBooksAdapter: BooksAdapter
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var bottomNavigationBarSequence: TapTargetSequence
    private lateinit var mainContentSequence: TapTargetSequence
    private var toolbarSequence: TapTargetSequence? = null
    private var bottomNavigationBarSequenceShown = false
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

        if (!viewModel.tutorialShown) {
            bottomNavigationBarSequence.cancel()
            mainContentSequence.cancel()
            toolbarSequence?.cancel()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchBooks()
        this.searchView?.clearFocus()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onItemClick(bookId: String) {

        val action = BooksFragmentDirections.actionBooksFragmentToBookDetailFragment(bookId, false)
        findNavController().navigate(action)
    }

    override fun onLoadMoreItemsClick() {}

    override fun onShowAllItemsClick(state: String) {

        val action = BooksFragmentDirections.actionBooksFragmentToBookListFragment(
            state,
            viewModel.sortParam,
            viewModel.isSortDescending,
            viewModel.query
        )
        findNavController().navigate(action)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menu.clear()
        menuInflater.inflate(R.menu.books_toolbar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return when (menuItem.itemId) {
//            R.id.action_synchronize -> {
//
//                openSyncPopup()
//                true
//            }
            R.id.action_sort -> {

                viewModel.sort(requireContext()) {
                    binding.apply {
                        recyclerViewReadingBooks.scrollToPosition(0)
                        recyclerViewPendingBooks.scrollToPosition(0)
                        recyclerViewBooks.scrollToPosition(0)
                    }
                }
                true
            }

            else -> false
        }
    }

    override fun onStartDragging(viewHolder: BooksViewHolder) {
    }

    override fun onFinishDragging(books: List<BookResponse>) {
        viewModel.setPriorityFor(books)
    }
    //endregion

    //region Public methods
    fun showAllBooks(view: View) {

        when (view) {
            binding.buttonShowAllPending -> {
                val action = BooksFragmentDirections.actionBooksFragmentToBookListFragment(
                    State.PENDING,
                    viewModel.sortParam,
                    viewModel.isSortDescending,
                    viewModel.query
                )
                findNavController().navigate(action)
            }

            binding.buttonShowAllRead -> {
                val action = BooksFragmentDirections.actionBooksFragmentToBookListFragment(
                    State.READ,
                    viewModel.sortParam,
                    viewModel.isSortDescending,
                    viewModel.query
                )
                findNavController().navigate(action)
            }
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        readingBooksAdapter = BooksAdapter(
            books = viewModel.books.value?.filter { it.isReading() }?.toMutableList()
                ?: mutableListOf(),
            isVerticalDesign = false,
            isGoogleBook = false,
            onItemClickListener = this
        )
        pendingBooksAdapter = BooksAdapter(
            books = viewModel.books.value?.filter { it.isPending() }?.toMutableList()
                ?: mutableListOf(),
            isVerticalDesign = true,
            isGoogleBook = false,
            onItemClickListener = this,
            onStartDraggingListener = this
        )
        booksAdapter = BooksAdapter(
            books = viewModel.books.value?.filter { !it.isReading() && !it.isPending() }?.toMutableList()
                ?: mutableListOf(),
            isVerticalDesign = true,
            isGoogleBook = false,
            onItemClickListener = this
        )
        setupBindings()

        with(binding) {

            recyclerViewReadingBooks.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerViewReadingBooks.adapter = readingBooksAdapter

            recyclerViewPendingBooks.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerViewPendingBooks.adapter = pendingBooksAdapter

            recyclerViewBooks.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerViewBooks.adapter = booksAdapter

            fragment = this@BooksFragment
            viewModel = this@BooksFragment.viewModel
            lifecycleOwner = this@BooksFragment
        }
        setupSearchView()
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.readingBooks.observe(viewLifecycleOwner) { booksResponse ->
            readingBooksAdapter.setBooks(booksResponse.toMutableList(), true)
        }

        viewModel.pendingBooks.observe(viewLifecycleOwner) { booksResponse ->

            val books = booksResponse.take(Constants.BOOKS_TO_SHOW).toMutableList()
            if (booksResponse.size > Constants.BOOKS_TO_SHOW) books.add(BookResponse(id = ""))
            pendingBooksAdapter.setBooks(books, true)
        }

        viewModel.readBooks.observe(viewLifecycleOwner) { booksResponse ->

            val books = booksResponse.take(Constants.BOOKS_TO_SHOW).toMutableList()
            if (booksResponse.size > Constants.BOOKS_TO_SHOW) books.add(BookResponse(id = ""))
            booksAdapter.setBooks(books, true)
        }

        viewModel.booksLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.booksError.observe(viewLifecycleOwner) {
            it?.let { manageError(it) }
        }
    }

    private fun setupSearchView() {

        this.searchView = binding.searchViewBooks
        this.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                viewModel.searchBooks(newText)
                binding.apply {
                    recyclerViewReadingBooks.scrollToPosition(0)
                    recyclerViewPendingBooks.scrollToPosition(0)
                    recyclerViewBooks.scrollToPosition(0)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                requireActivity().hideSoftKeyboard()
                searchView?.clearFocus()
                return true
            }
        })
        this.setupSearchView(R.color.colorSecondary, "")
    }

    private fun createTargetsForBottomNavigationView(): List<TapTarget> {
        return listOf(
            Constants.createTargetForBottomNavigationView(
                activity,
                R.id.nav_graph_books,
                resources.getString(R.string.books_view_tutorial_title),
                resources.getString(R.string.books_view_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true),
            Constants.createTargetForBottomNavigationView(
                activity,
                R.id.nav_graph_search,
                resources.getString(R.string.search_view_tutorial_title),
                resources.getString(R.string.search_view_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true),
            Constants.createTargetForBottomNavigationView(
                activity,
                R.id.nav_graph_stats,
                resources.getString(R.string.stats_view_tutorial_title),
                resources.getString(R.string.stats_view_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true),
            Constants.createTargetForBottomNavigationView(
                activity,
                R.id.nav_graph_settings,
                resources.getString(R.string.settings_view_tutorial_title),
                resources.getString(R.string.settings_view_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true)
        )
    }

    private fun createTargetsForToolbar(): List<TapTarget> {

//        val syncItem = binding.toolbar.menu.findItem(R.id.action_synchronize)
        val sortItem = binding.toolbar.menu.findItem(R.id.action_sort)
        return listOf(
//            TapTarget.forToolbarMenuItem(
//                binding.toolbar,
//                syncItem.itemId,
//                resources.getString(R.string.sync_icon_tutorial_title),
//                resources.getString(R.string.sync_icon_tutorial_description)
//            ).style(requireContext()).cancelable(true).tintTarget(true),
            TapTarget.forToolbarMenuItem(
                binding.toolbar,
                sortItem.itemId,
                resources.getString(R.string.sort_icon_tutorial_title),
                resources.getString(R.string.sort_icon_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(true)
        )
    }

    private fun createTargetsForScrollView(): List<TapTarget> {
        return listOf(
            TapTarget.forView(
                binding.searchViewBooks,
                resources.getString(R.string.search_bar_books_tutorial_title),
                resources.getString(R.string.search_bar_books_tutorial_description)
            ).style(requireContext()).cancelable(true).tintTarget(false)
        )
    }

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            bottomNavigationBarSequence = TapTargetSequence(requireActivity()).apply {
                targets(createTargetsForBottomNavigationView())
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        bottomNavigationBarSequenceShown = true
                        mainContentSequence.start()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                if (!bottomNavigationBarSequenceShown) {
                    start()
                }
            }

            mainContentSequence = TapTargetSequence(requireActivity()).apply {
                targets(createTargetsForScrollView())
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        mainContentSequenceShown = true
                        toolbarSequence?.start()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                if (bottomNavigationBarSequenceShown && !mainContentSequenceShown) {
                    start()
                }
            }
            /*
            Must be created with a delay in order to wait for the fragment menu creation,
            otherwise it wouldn't be icons in the toolbar
             */
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                toolbarSequence = TapTargetSequence(requireActivity()).apply {
                    targets(createTargetsForToolbar())
                    continueOnCancel(false)
                    listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            viewModel.setTutorialAsShown()
                        }

                        override fun onSequenceStep(
                            lastTarget: TapTarget,
                            targetClicked: Boolean
                        ) {
                        }

                        override fun onSequenceCanceled(lastTarget: TapTarget) {}
                    })
                    if (bottomNavigationBarSequenceShown && mainContentSequenceShown) {
                        start()
                    }
                }
            }
        }
    }
    //endregion
}