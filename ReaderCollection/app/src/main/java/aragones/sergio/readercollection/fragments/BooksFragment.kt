/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.extensions.handleStatusBar
import aragones.sergio.readercollection.extensions.hideSoftKeyboard
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import kotlinx.android.synthetic.main.fragment_books.*

class BooksFragment : BaseFragment(), OnItemClickListener {

    //MARK: - Private properties

    private lateinit var ibSynchronize: ImageButton
    private lateinit var ibSort: ImageButton
    private lateinit var tvSubtitle: TextView
    private lateinit var svBooks: SearchView
    private lateinit var rvReadingBooks: RecyclerView
    private lateinit var vwSeparatorReadingPending: View
    private lateinit var ivNoReadingBooks: ImageView
    private lateinit var tvPendingBooks: TextView
    private lateinit var btSeeMorePendingBooks: Button
    private lateinit var rvPendingBooks: RecyclerView
    private lateinit var vwSeparatorPendingRead: View
    private lateinit var tvReadBooks: TextView
    private lateinit var btSeeMoreReadBooks: Button
    private lateinit var rvBooks: RecyclerView
    private lateinit var vwNoResults: View

    private lateinit var viewModel: BooksViewModel
    private lateinit var readingBooksAdapter: BooksAdapter
    private lateinit var pendingBooksAdapter: BooksAdapter
    private lateinit var booksAdapter: BooksAdapter

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    override fun onResume() {
        super.onResume()

        activity?.handleStatusBar(requireView(), true, activity?.isDarkMode() == true)
        if (this::viewModel.isInitialized) viewModel.getBooks()
        svBooks.clearFocus()
    }

    override fun onPause() {
        super.onPause()

        activity?.handleStatusBar(requireView(), false, activity?.isDarkMode() == false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }

    //MARK: - Interface methods

    override fun onItemClick(bookId: String) {

        val params = mapOf(Constants.BOOK_ID to bookId, Constants.IS_GOOGLE_BOOK to false)
        launchActivityWithExtras(BookDetailActivity::class.java, params)
    }

    override fun onLoadMoreItemsClick() {}

    //MARK: - Private methods

    private fun initializeUI() {

        ibSynchronize = image_button_synchronize
        ibSort = image_button_sort
        tvSubtitle = text_view_subtitle
        svBooks = search_view_books
        rvReadingBooks = recycler_view_reading_books
        vwSeparatorReadingPending = view_separator_reading_pending
        ivNoReadingBooks = image_view_no_reading_results
        tvPendingBooks = text_view_pending_books
        btSeeMorePendingBooks = button_see_more_pending
        rvPendingBooks = recycler_view_pending_books
        vwSeparatorPendingRead = view_separator_pending_read
        tvReadBooks = text_view_read_books
        btSeeMoreReadBooks = button_see_more_read
        rvBooks = recycler_view_books
        vwNoResults = content_view_no_results

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            BooksViewModelFactory(application)
        )[BooksViewModel::class.java]
        readingBooksAdapter = BooksAdapter(
            viewModel.books.value?.filter { it.state == State.READING }?.toMutableList()
                ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        pendingBooksAdapter = BooksAdapter(
            viewModel.books.value?.filter { it.state == State.PENDING }?.toMutableList()
                ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        booksAdapter = BooksAdapter(
            viewModel.books.value?.filter {
                it.state != State.READING && it.state != State.PENDING
            }?.toMutableList() ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        setupBindings()

        ibSynchronize.setOnClickListener {
            openSyncPopup()
        }

        ibSort.setOnClickListener {
            viewModel.sort(
                requireContext(),
                resources.getStringArray(R.array.sorting_keys_ids),
                resources.getStringArray(R.array.sorting_keys)
            )
        }

        setupSearchView()

        btSeeMorePendingBooks.setOnClickListener {
            //TODO: implement
        }

        btSeeMoreReadBooks.setOnClickListener {
            //TODO: implement
        }

        rvReadingBooks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvReadingBooks.adapter = readingBooksAdapter

        rvPendingBooks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvPendingBooks.adapter = pendingBooksAdapter

        rvBooks.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvBooks.adapter = booksAdapter
    }

    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner, { booksResponse ->

            setTitle(booksResponse.size)
            if (booksResponse.isEmpty()) {
                ivNoReadingBooks.visibility = View.GONE
                vwSeparatorReadingPending.visibility = View.GONE
            }
            vwNoResults.visibility = if (booksResponse.isEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.readingBooks.observe(viewLifecycleOwner, { booksResponse ->

            readingBooksAdapter.resetList()
            readingBooksAdapter.setBooks(booksResponse.toMutableList())
            rvReadingBooks.scrollToPosition(0)
            val hideReadingSection =
                (booksResponse.isEmpty() && viewModel.query.isNotBlank()) || viewModel.books.value?.isEmpty() == true
            rvReadingBooks.visibility = if (hideReadingSection) View.GONE else View.VISIBLE
            ivNoReadingBooks.visibility =
                if (hideReadingSection || booksResponse.isNotEmpty()) View.GONE else View.VISIBLE
            vwSeparatorReadingPending.visibility =
                if (hideReadingSection) View.GONE else View.VISIBLE
        })

        viewModel.pendingBooks.observe(viewLifecycleOwner, { booksResponse ->

            pendingBooksAdapter.resetList()
            pendingBooksAdapter.setBooks(booksResponse.toMutableList())
            rvPendingBooks.scrollToPosition(0)
            tvPendingBooks.visibility = if (booksResponse.isEmpty()) View.GONE else View.VISIBLE
            rvPendingBooks.visibility = if (booksResponse.isEmpty()) View.GONE else View.VISIBLE
            vwSeparatorPendingRead.visibility =
                if (booksResponse.isEmpty()) View.GONE else View.VISIBLE
        })

        viewModel.readBooks.observe(viewLifecycleOwner, { booksResponse ->

            booksAdapter.resetList()
            booksAdapter.setBooks(booksResponse.toMutableList())
            rvBooks.scrollToPosition(0)
            tvReadBooks.visibility = if (booksResponse.isEmpty()) View.GONE else View.VISIBLE
            rvBooks.visibility = if (booksResponse.isEmpty()) View.GONE else View.VISIBLE
        })

        viewModel.booksLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.booksError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })
    }

    private fun setTitle(booksCount: Int) {

        val title = resources.getQuantityString(R.plurals.title_books_count, booksCount, booksCount)
        tvSubtitle.text = title
        (activity as AppCompatActivity?)?.supportActionBar?.title = ""
    }

    private fun setupSearchView() {

        svBooks.isIconified = false
        svBooks.isIconifiedByDefault = false

        val searchIconId = svBooks.context.resources.getIdentifier(
            "android:id/search_mag_icon",
            null,
            null
        )
        val color = ContextCompat.getColor(requireActivity(), R.color.colorSecondary)
        svBooks.findViewById<AppCompatImageView>(searchIconId)?.imageTintList =
            ColorStateList.valueOf(color)

        val searchPlateId = svBooks.context.resources.getIdentifier(
            "android:id/search_plate",
            null,
            null
        )
        val searchPlate = svBooks.findViewById<View>(searchPlateId)
        if (searchPlate != null) {

            searchPlate.setBackgroundColor(Color.TRANSPARENT)
            val searchTextId = searchPlate.context.resources.getIdentifier(
                "android:id/search_src_text",
                null,
                null
            )
            val searchText = searchPlate.findViewById<TextView>(searchTextId)
            if (searchText != null) {

                searchText.setTextColor(color)
                searchText.setHintTextColor(color)
            }

            val searchCloseId = searchPlate.context.resources.getIdentifier(
                "android:id/search_close_btn",
                null,
                null
            )
            searchPlate.findViewById<AppCompatImageView>(searchCloseId)?.imageTintList =
                ColorStateList.valueOf(color)
        }

        svBooks.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                viewModel.searchBooks(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                requireActivity().hideSoftKeyboard()
                svBooks.clearFocus()
                return true
            }
        })
    }
}