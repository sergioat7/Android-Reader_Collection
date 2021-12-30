/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
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
import aragones.sergio.readercollection.viewholders.BooksViewHolder
import aragones.sergio.readercollection.viewmodelfactories.BooksViewModelFactory
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_books.*
import kotlin.math.max
import kotlin.math.min

class BooksFragment : BaseFragment(), OnItemClickListener {

    //MARK: - Private properties

    private lateinit var ibSynchronize: ImageButton
    private lateinit var ibSort: ImageButton
    private lateinit var tvSubtitle: TextView
    private lateinit var svBooks: SearchView
    private lateinit var pbLoadingFormats: ProgressBar
    private lateinit var spFormats: Spinner
    private lateinit var pbLoadingStates: ProgressBar
    private lateinit var spStates: Spinner
    private lateinit var spFavourite: Spinner
    private lateinit var vwSeparator: View
    private lateinit var rvBooks: RecyclerView
    private lateinit var ivNoResults: View
    private lateinit var fbStartList: FloatingActionButton
    private lateinit var fbEndList: FloatingActionButton
    private lateinit var viewModel: BooksViewModel
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var formatValues: MutableList<String>
    private lateinit var stateValues: MutableList<String>
    private lateinit var favouriteValues: List<String>
    private val scrollPosition = MutableLiveData<ScrollPosition>()

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
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        if (this::viewModel.isInitialized) viewModel.getBooks()
        svBooks.clearFocus()
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
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
        pbLoadingFormats = progress_bar_loading_formats
        spFormats = spinner_formats
        pbLoadingStates = progress_bar_loading_states
        spStates = spinner_states
        spFavourite = spinner_favourite
        vwSeparator = view_separator
        rvBooks = recycler_view_books
        ivNoResults = image_view_no_results
        fbStartList = floating_action_button_start_list
        fbEndList = floating_action_button_end_list
        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            BooksViewModelFactory(application)
        )[BooksViewModel::class.java]
        booksAdapter = BooksAdapter(
            viewModel.books.value?.toMutableList() ?: mutableListOf(),
            false,
            requireContext(),
            this
        )
        favouriteValues = resources.getStringArray(R.array.favourites).toList()
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

        spFormats.onItemSelectedListener = object : OnItemSelectedListener {
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

        spStates.onItemSelectedListener = object : OnItemSelectedListener {
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

        val pos = when (viewModel.isFavourite.value) {
            true -> 1
            false -> 2
            else -> 0
        }
        spFavourite.setSelection(pos)
        spFavourite.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {

                val isFavourite = when (favouriteValues[position]) {
                    resources.getString(R.string.yes) -> true
                    resources.getString(R.string.no) -> false
                    else -> null
                }
                viewModel.setFavourite(isFavourite)
                viewModel.getBooks()
                spFavourite.requestLayout()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = booksAdapter
        rvBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                scrollPosition.value =
                    if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        ScrollPosition.TOP
                    } else if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        ScrollPosition.END
                    } else {
                        ScrollPosition.MIDDLE
                    }
            }
        })
        ItemTouchHelper(SwipeController()).attachToRecyclerView(rvBooks)

        scrollPosition.value = ScrollPosition.TOP

        fbStartList.setOnClickListener {

            rvBooks.scrollToPosition(0)
            scrollPosition.value = ScrollPosition.TOP
        }

        fbEndList.setOnClickListener {

            val position: Int = booksAdapter.itemCount - 1
            rvBooks.scrollToPosition(position)
            scrollPosition.value = ScrollPosition.END
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

            val selectedFormatName =
                getSelectedValue(viewModel.formats, viewModel.selectedFormat)?.name
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

            val selectedStateName =
                getSelectedValue(viewModel.states, viewModel.selectedState)?.name
            spStates.setSelection(
                stateValues.indexOf(selectedStateName)
            )
        })

        viewModel.booksFormatsLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingFormats.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.booksStatesLoading.observe(viewLifecycleOwner, { isLoading ->
            pbLoadingStates.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.bookSet.observe(viewLifecycleOwner, { position ->
            position?.let {
                booksAdapter.notifyItemChanged(position)
            }
        })

        viewModel.bookDeleted.observe(viewLifecycleOwner, { position ->
            position?.let {
                booksAdapter.notifyItemRemoved(position)
            }
        })

        viewModel.booksError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })

        scrollPosition.observe(viewLifecycleOwner, {

            vwSeparator.visibility = if (it == ScrollPosition.TOP) View.GONE else View.VISIBLE
            fbStartList.visibility = if (it == ScrollPosition.TOP) View.GONE else View.VISIBLE
            fbEndList.visibility = if (it == ScrollPosition.END) View.GONE else View.VISIBLE
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

    private fun createSpinner(
        spinner: Spinner,
        values: List<String>,
        title: String
    ) {

        spinner.adapter = Constants.getAdapter(
            context = requireContext(),
            data = values,
            firstOptionEnabled = true,
            rounded = true,
            title = title
        )
    }

    private fun <T : BaseModel<String>> getSelectedValue(
        values: LiveData<List<T>>,
        selectedValue: LiveData<String?>
    ): T? {
        return values.value?.firstOrNull { it.id == selectedValue.value }
    }

    private fun setTitle(booksCount: Int) {

        val title = resources.getQuantityString(R.plurals.title_books_count, booksCount, booksCount)
        tvSubtitle.text = title
        (activity as AppCompatActivity?)?.supportActionBar?.title = ""
    }

    private fun setupSearchView() {
        svBooks.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                viewModel.searchBooks(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                Constants.hideSoftKeyboard(requireActivity())
                svBooks.clearFocus()
                return true
            }
        })
    }

    enum class ScrollPosition {
        TOP, MIDDLE, END
    }

    inner class SwipeController :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val paint = Paint()

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val position = viewHolder.adapterPosition
            if (direction == ItemTouchHelper.LEFT) {
                showPopupConfirmationDialog(R.string.book_remove_confirmation, acceptHandler = {
                    viewModel.deleteBook(position)
                }, {
                    booksAdapter.notifyItemChanged(position)
                })
            } else {
                viewModel.setBookFavourite(position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            var x = dX
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                val itemView = viewHolder.itemView
                val context = recyclerView.context

                val height = itemView.bottom - itemView.top
                val width = height / 3
                val maxX = itemView.width.toFloat() * 0.6F

                val isFavourite = (viewHolder as BooksViewHolder).isFavourite
                val leftId =
                    when {
                        isFavourite && Constants.isDarkMode(context) -> R.drawable.ic_favourite_empty_dark
                        isFavourite && !Constants.isDarkMode(context) -> R.drawable.ic_favourite_empty_light
                        !isFavourite && Constants.isDarkMode(context) -> R.drawable.ic_favourite_full_dark
                        else -> R.drawable.ic_favourite_full_light
                    }
                val rightId =
                    if (Constants.isDarkMode(context)) R.drawable.ic_remove_book_dark
                    else R.drawable.ic_remove_book_light

                when {
                    dX > 0 -> {// Swiping to the right
                        paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
                        val background = RectF(
                            itemView.left.toFloat(),
                            itemView.top.toFloat(),
                            dX,
                            itemView.bottom.toFloat()
                        )
                        c.drawRect(background, paint)
                        val icon = ContextCompat.getDrawable(context, leftId)
                        icon?.setBounds(
                            itemView.left + width,
                            itemView.top + width,
                            itemView.left + 2 * width,
                            itemView.bottom - width
                        )
                        icon?.draw(c)
                        x = min(dX, maxX)
                    }
                    dX < 0 -> {// Swiping to the left
                        paint.color = ContextCompat.getColor(context, R.color.colorTertiary)
                        val background = RectF(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        c.drawRect(background, paint)

                        val icon = ContextCompat.getDrawable(context, rightId)
                        icon?.setBounds(
                            itemView.right - 2 * width,
                            itemView.top + width,
                            itemView.right - width,
                            itemView.bottom - width
                        )
                        icon?.draw(c)
                        x = max(dX, -maxX)
                    }
                    else -> {// view is unSwiped
                        val background = RectF(0F, 0F, 0F, 0F)
                        c.drawRect(background, paint)
                    }
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, x, dY, actionState, isCurrentlyActive)
        }
    }
}