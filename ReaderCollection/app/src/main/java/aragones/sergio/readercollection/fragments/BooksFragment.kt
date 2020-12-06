/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.BookDetailActivity
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.models.base.BaseModel
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_books.*

class BooksFragment: BaseFragment(), OnItemClickListener {

    //MARK: - Private properties

    private lateinit var pbLoadingFormats: ProgressBar
    private lateinit var spFormats: Spinner
    private lateinit var pbLoadingStates: ProgressBar
    private lateinit var spStates: Spinner
    private lateinit var spFavourite: Spinner
    private lateinit var srlBooks: SwipeRefreshLayout
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivNoResults: View
    private lateinit var fbStartList: FloatingActionButton
    private lateinit var fbEndList: FloatingActionButton
    private lateinit var viewModel: BooksViewModel
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var formatValues: MutableList<String>
    private lateinit var stateValues: MutableList<String>
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

    override fun onResume() {
        super.onResume()
        viewModel.getBooks()
    }

    //MARK: - Interface methods

    override fun onItemClick(bookId: String) {

        val params = mapOf(Constants.BOOK_ID to bookId, Constants.IS_GOOGLE_BOOK to false)
        launchActivityWithExtras(BookDetailActivity::class.java, params)
    }

    override fun onLoadMoreItemsClick() {}

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
        fbStartList = floating_action_button_start_list
        fbEndList = floating_action_button_end_list
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, BooksViewModelFactory(application)).get(BooksViewModel::class.java)
        booksAdapter = BooksAdapter(
            viewModel.books.value?.toMutableList() ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        favouriteValues = resources.getStringArray(R.array.favourites).toList()
        setupBindings()

        spFormats.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {

                val formatName = formatValues[position]
                val formatId = viewModel.formats.value?.firstOrNull { it.name == formatName }?.id
                viewModel.setFormat(formatId)
                viewModel.getBooks()
                spFormats.requestLayout()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        spStates.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {

                val stateName = stateValues[position]
                val stateId = viewModel.states.value?.firstOrNull { it.name == stateName }?.id
                viewModel.setState(stateId)
                viewModel.getBooks()
                spStates.requestLayout()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        createSpinner(
            spFavourite,
            favouriteValues,
            resources.getString(R.string.favourite)
        )

        val pos = when(viewModel.isFavourite.value) {
            true -> 1
            false -> 2
            else -> 0
        }
        spFavourite.setSelection(pos)
        spFavourite.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {

                val favouriteValue = favouriteValues[position]
                val isFavourite = if(favouriteValue == resources.getString(R.string.yes)) true else if(favouriteValue == resources.getString(R.string.no)) false else null
                viewModel.setFavourite(isFavourite)
                viewModel.getBooks()
                spFavourite.requestLayout()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        srlBooks.setOnRefreshListener {

            viewModel.reloadData()
            viewModel.getBooks()
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

        fbEndList.setOnClickListener {

            val position: Int = booksAdapter.itemCount - 1
            rvBooks.scrollToPosition(position)
            fbStartList.visibility = View.VISIBLE
            fbEndList.visibility = View.GONE
        }
    }

    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner, { booksResponse ->

            ivNoResults.visibility = if (booksResponse.isEmpty()) View.VISIBLE else View.GONE
            booksAdapter.resetList()
            booksAdapter.setBooks(booksResponse.toMutableList())
            setTitle(booksResponse.size)
        })

        viewModel.formats.observe(viewLifecycleOwner, { formatsResponse ->

            fillFormats(formatsResponse)
            createSpinner(
                spFormats,
                formatValues,
                resources.getString(R.string.format)
            )

            val selectedFormatName = getSelectedValue(viewModel.formats, viewModel.selectedFormat)?.name
            spFormats.setSelection(
                formatValues.indexOf(selectedFormatName)
            )
        })

        viewModel.states.observe(viewLifecycleOwner, { statesResponse ->

            fillStates(statesResponse)
            createSpinner(
                spStates,
                stateValues,
                resources.getString(R.string.state)
            )

            val selectedStateName = getSelectedValue(viewModel.states, viewModel.selectedState)?.name
            spStates.setSelection(
                stateValues.indexOf(selectedStateName)
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

    private fun fillFormats(formatsResponse: List<FormatResponse>?) {

        formatValues = mutableListOf()
        formatValues.add(resources.getString((R.string.anything)))
        formatsResponse?.let {
            formatValues.addAll(formatsResponse.map { it.name })
        }
    }

    private fun fillStates(statesResponse: List<StateResponse>?) {

        stateValues = mutableListOf()
        stateValues.add(resources.getString((R.string.anything)))
        statesResponse?.let {
            stateValues.addAll(statesResponse.map { it.name })
        }
    }

    private fun createSpinner(spinner: Spinner,
                              values: List<String>,
                              title: String) {

        spinner.adapter = Constants.getAdapter(
            context = requireContext(),
            data = values,
            firstOptionEnabled = true,
            rounded = true,
            title = title)
    }

    private fun <T: BaseModel<String>> getSelectedValue(values: LiveData<List<T>>,
                                                        selectedValue: LiveData<String?>): T? {
        return values.value?.firstOrNull { it.id == selectedValue.value }
    }

    private fun setTitle(booksCount: Int) {

        val title = when(booksCount) {
            1 -> resources.getString(R.string.title_book_count, booksCount)
            else -> resources.getString(R.string.title_books_count, booksCount)
        }
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }
}