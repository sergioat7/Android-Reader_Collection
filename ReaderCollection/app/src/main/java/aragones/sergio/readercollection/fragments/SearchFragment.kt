/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.app.SearchManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment: BaseFragment(), OnItemClickListener {

    //MARK: - Private properties

    private lateinit var srlBooks: SwipeRefreshLayout
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivNoResults: View
    private lateinit var fbStartList: FloatingActionButton
    private lateinit var fbEndList: FloatingActionButton
    private lateinit var viewModel: SearchViewModel
    private lateinit var booksAdapter: BooksAdapter

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

    //MARK: - Interface methods

    override fun onItemClick(bookId: String) {

        val params = mapOf(Constants.BOOK_ID to bookId, Constants.IS_GOOGLE_BOOK to true)
        launchActivityWithExtras(BookDetailActivity::class.java, params)
    }

    override fun onLoadMoreItemsClick() {
        viewModel.searchBooks()
    }

    //MARK: - Public methods

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.search_toolbar_menu, menu)
        setupSearchView(menu)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        srlBooks = swipe_refresh_layout_books
        rvBooks = recycler_view_books
        ivNoResults = image_view_no_results
        fbStartList = floating_action_button_start_list
        fbEndList = floating_action_button_end_list
        viewModel = ViewModelProvider(this, SearchViewModelFactory(application)).get(SearchViewModel::class.java)
        booksAdapter = BooksAdapter(
            viewModel.books.value ?: mutableListOf(),
            true,
            requireContext(),
            this
        )
        setupBindings()

        srlBooks.setOnRefreshListener {

            viewModel.reloadData()
            viewModel.searchBooks()
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = booksAdapter
        rvBooks.addOnScrollListener(object: RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                fbStartList.visibility =
                    if (!recyclerView.canScrollVertically(-1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }

                fbEndList.visibility =
                    if (!recyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
        })

        fbStartList.visibility = View.GONE
        fbStartList.setOnClickListener {

            rvBooks.scrollToPosition(0)
            fbStartList.visibility = View.GONE
            fbEndList.visibility = View.VISIBLE
        }

        fbEndList.visibility = View.GONE
        fbEndList.setOnClickListener {

            val position: Int = booksAdapter.itemCount - 1
            rvBooks.scrollToPosition(position)
            fbStartList.visibility = View.VISIBLE
            fbEndList.visibility = View.GONE
        }

        if (viewModel.query.isNotBlank()) {
            (activity as AppCompatActivity?)?.supportActionBar?.title = resources.getString(R.string.query_title, viewModel.query)
        }
    }

    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner, { booksResponse ->

            if (booksResponse.isEmpty()) {

                booksAdapter.resetList()
                ivNoResults.visibility = View.VISIBLE
            } else {

                booksAdapter.setBooks(booksResponse)
                ivNoResults.visibility = View.GONE
                fbEndList.visibility = View.VISIBLE
            }
        })

        viewModel.searchLoading.observe(viewLifecycleOwner, { isLoading ->
            srlBooks.isRefreshing = isLoading
        })

        viewModel.searchError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })
    }

    private fun setupSearchView(menu: Menu) {
        
        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (searchManager != null) {

            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            searchView.isIconified = false
            searchView.isIconifiedByDefault = false
            searchView.queryHint = resources.getString(R.string.search_books)
            if (viewModel.query.isNotBlank()) {
                searchView.setQuery(viewModel.query, false)
            }
            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {

                    searchBooks(query)
                    menuItem.collapseActionView()
                    return true
                }
            })
        }

        val color = ContextCompat.getColor(requireActivity(), R.color.textTertiary)

        val searchIconId = searchView.context.resources.getIdentifier(
            "android:id/search_mag_icon",
            null,
            null
        )
        searchView.findViewById<AppCompatImageView>(searchIconId)?.imageTintList = ColorStateList.valueOf(color)

        val searchPlateId = searchView.context.resources.getIdentifier(
            "android:id/search_plate",
            null,
            null
        )
        val searchPlate = searchView.findViewById<View>(searchPlateId)
        if (searchPlate != null) {

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
            searchPlate.findViewById<AppCompatImageView>(searchCloseId)?.imageTintList = ColorStateList.valueOf(color)
        }
    }

    private fun searchBooks(query: String) {

        viewModel.setSearch(query)
        viewModel.reloadData()
        viewModel.searchBooks()
        Constants.hideSoftKeyboard(requireActivity())
        (activity as AppCompatActivity?)?.supportActionBar?.title = resources.getString(R.string.query_title, query)
    }
}