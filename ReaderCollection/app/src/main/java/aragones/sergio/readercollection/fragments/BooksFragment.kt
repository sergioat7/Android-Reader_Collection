/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import kotlinx.android.synthetic.main.fragment_books.*


class BooksFragment: BaseFragment(), BooksAdapter.OnItemClickListener {

    //MARK: - Private properties

    private lateinit var pbLoadingFormats: ProgressBar
    private lateinit var spFormats: Spinner
    private lateinit var pbLoadingStates: ProgressBar
    private lateinit var spStates: Spinner
    private lateinit var spFavourite: Spinner
    private lateinit var srlBooks: SwipeRefreshLayout
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivNoResults: View
    private lateinit var viewModel: BooksViewModel
    private lateinit var booksAdapter: BooksAdapter
    private var formats: List<FormatResponse>? = null
    private var formatValues = ArrayList<String>()
    private var states: List<StateResponse>? = null
    private var stateValues = ArrayList<String>()
    private lateinit var favouriteValues: List<String>

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

        pbLoadingFormats = progress_bar_loading_formats
        spFormats = spinner_formats
        pbLoadingStates = progress_bar_loading_states
        spStates = spinner_states
        spFavourite = spinner_favourite
        srlBooks = swipe_refresh_layout_books
        rvBooks = recycler_view_books
        ivNoResults = image_view_no_results
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BooksViewModelFactory(application)).get(BooksViewModel::class.java)
        booksAdapter = BooksAdapter(
            viewModel.books.value ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        favouriteValues = resources.getStringArray(R.array.favourites).toList()
        setupBindings()

        spFormats.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                val formatName = formatValues[position]
                val formatId = formats?.firstOrNull { it.name == formatName }?.id
                viewModel.setFormat(formatId)
                viewModel.getBooks()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        spStates.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                val stateName = stateValues[position]
                val stateId = states?.firstOrNull { it.name == stateName }?.id
                viewModel.setState(stateId)
                viewModel.getBooks()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        spFavourite.adapter = Constants.getAdapter(requireContext(), favouriteValues, true, true)
        spFavourite.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                R.color.colorPrimary
            )
        )

        spFavourite.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                val favouriteValue = favouriteValues[position]
                val isFavourite = if(favouriteValue == resources.getString(R.string.yes)) true else if(favouriteValue == resources.getString(R.string.no)) false else null
                viewModel.setFavourite(isFavourite)
                viewModel.getBooks()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        srlBooks.setOnRefreshListener {

            viewModel.reloadData()
            viewModel.getBooks()
        }
        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = booksAdapter

        viewModel.getBooks()
        viewModel.getFormats()
        viewModel.getStates()
    }

    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner, { booksResponse ->

            ivNoResults.visibility = if (booksResponse.isEmpty()) View.VISIBLE else View.GONE
            booksAdapter.resetList()
            booksAdapter.addBooks(booksResponse)
        })

        viewModel.formats.observe(viewLifecycleOwner, { formatsResponse ->

            formats = formatsResponse
            formatValues = ArrayList()
            formatValues.run {

                this.add(resources.getString((R.string.none)))
                this.addAll(formatsResponse.map { it.name })
            }
            spFormats.adapter = Constants.getAdapter(requireContext(), formatValues, true, true)
            spFormats.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.colorPrimary
                )
            )
        })

        viewModel.states.observe(viewLifecycleOwner, { statesResponse ->

            states = statesResponse
            stateValues = ArrayList()
            stateValues.run {

                this.add(resources.getString((R.string.none)))
                this.addAll(statesResponse.map { it.name })
            }
            spStates.adapter = Constants.getAdapter(requireContext(), stateValues, true, true)
            spStates.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.colorPrimary
                )
            )
        })

        viewModel.booksLoading.observe(viewLifecycleOwner, { isLoading ->
            srlBooks.isRefreshing = isLoading
        })

        viewModel.booksFormatsLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingFormats.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.booksStatesLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingStates.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.booksError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })
    }
}