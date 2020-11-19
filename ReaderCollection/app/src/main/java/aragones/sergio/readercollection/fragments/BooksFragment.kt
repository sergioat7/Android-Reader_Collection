/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import kotlinx.android.synthetic.main.fragment_books.*

class BooksFragment: BaseFragment(), BooksAdapter.OnItemClickListener {

    //MARK: - Private properties

    private lateinit var srlBooks: SwipeRefreshLayout
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivNoResults: View
    private lateinit var viewModel: BooksViewModel
    private lateinit var booksAdapter: BooksAdapter

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    //MARK: - Interface methods

    override fun onItemClick(bookId: String) {

        val params = mapOf(Constants.BOOK_ID to bookId, Constants.IS_GOOGLE_BOOK to false)
        launchActivityWithExtras(BookDetailActivity::class.java, params)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        srlBooks = swipe_refresh_layout_books
        rvBooks = recycler_view_books
        ivNoResults = image_view_no_results
        viewModel = ViewModelProvider(this, BooksViewModelFactory(application)).get(BooksViewModel::class.java)
        booksAdapter = BooksAdapter(
            /*viewModel.books.value ?: */mutableListOf(),
            requireContext(),
            this
        )
        setupBindings()

        srlBooks.setOnRefreshListener {

            //TODO reload data
            //TODO get books
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = booksAdapter
        rvBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //TODO get books
                }
            }
        })
    }

    private fun setupBindings() {
        //TODO use ViewModel
    }
}