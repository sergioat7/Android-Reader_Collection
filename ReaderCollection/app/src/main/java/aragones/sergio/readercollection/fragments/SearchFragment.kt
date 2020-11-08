/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import aragones.sergio.readercollection.viewmodels.SearchViewModel

class SearchFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var viewModel: SearchViewModel

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
        viewModel = ViewModelProvider(this, SearchViewModelFactory(application)).get(SearchViewModel::class.java)
        setupBindings()
    }

    private fun setupBindings() {

        viewModel.googleBooks.observe(viewLifecycleOwner, { googleBooksResponse ->

            if (googleBooksResponse.isEmpty()) {
                //TODO reset list
                //TODO show no books image
            } else {
                //TODO add books to list
                //TODO hide no books image
            }
        })

        viewModel.searchLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })
    }

    private fun setupSearchView(menu: Menu) {

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            searchView.isIconified = false
            searchView.isIconifiedByDefault = false
            searchView.queryHint = resources.getString(R.string.search_books)
            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {

                    searchBooks(query)
                    return true
                }
            })
        }
        val searchPlateId = searchView.context.resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchView.findViewById<View>(searchPlateId)
        if (searchPlate != null) {

            val searchTextId = searchPlate.context.resources.getIdentifier(
                "android:id/search_src_text",
                null,
                null
            )
            val searchText = searchPlate.findViewById<TextView>(searchTextId)
            if (searchText != null) {

                searchText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorSecondary))
                searchText.setHintTextColor(ContextCompat.getColor(requireActivity(),R.color.colorSecondary))
            }
        }
    }

    private fun searchBooks(query: String) {

        viewModel.setSearch(query)
        viewModel.reloadData()
        viewModel.searchBooks()
        Constants.hideSoftKeyboard(requireActivity())
    }
}