/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BookDetailViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel

class BookDetailFragment: BaseFragment() {

    //MARK: - Private properties

    private var bookId: String = ""
    private var isGoogleBook: Boolean = false
    private lateinit var viewModel: BookDetailViewModel

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
        return inflater.inflate(R.layout.book_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.book_detail_toolbar_menu, menu)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BookDetailViewModelFactory(application)).get(BookDetailViewModel::class.java)
        viewModel.setBookId(bookId)
        viewModel.setIsGoogleBook(isGoogleBook)
        setupBindings()

        viewModel.getBook()
    }

    private fun setupBindings() {

        viewModel.book.observe(viewLifecycleOwner, {
            showData(it)
        })

        viewModel.bookDetailLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.bookDetailError.observe(viewLifecycleOwner, {
            manageError(it)
        })
    }

    private fun showData(bookResponse: BookResponse) {
        //TODO show data
    }
}