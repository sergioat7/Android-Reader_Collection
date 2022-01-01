/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.math.max

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
        if (this::viewModel.isInitialized) viewModel.onDestroy()
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

        viewModel = ViewModelProvider(this, SearchViewModelFactory(application))[SearchViewModel::class.java]
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
        ItemTouchHelper(SwipeController()).attachToRecyclerView(rvBooks)

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

        viewModel.bookAdded.observe(viewLifecycleOwner, { position ->
            position?.let {

                val message = resources.getString(R.string.book_saved)
                showPopupDialog(message)
            }
        })

        viewModel.searchError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })
    }

    private fun setupSearchView(menu: Menu) {

        val menuItem = menu.findItem(R.id.action_search)
        this.searchView = menuItem.actionView as SearchView
        this.searchView?.let { searchView ->

            searchView.queryHint = resources.getString(R.string.search)
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
        this.setupSearchView(viewModel.query)
    }

    private fun searchBooks(query: String) {

        viewModel.setSearch(query)
        viewModel.reloadData()
        viewModel.searchBooks()
        Constants.hideSoftKeyboard(requireActivity())
        (activity as AppCompatActivity?)?.supportActionBar?.title = resources.getString(R.string.query_title, query)
    }

    inner class SwipeController :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        private val paint = Paint()

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val position = viewHolder.adapterPosition
            viewModel.addBook(position)
            booksAdapter.notifyItemChanged(position)
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

                val iconId =
                    if (Constants.isDarkMode(context)) R.drawable.ic_save_book_dark
                    else R.drawable.ic_save_book_light

                when {
                    dX < 0 -> {// Swiping to the left
                        paint.color = ContextCompat.getColor(context, R.color.colorTertiary)
                        val background = RectF(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat()
                        )
                        c.drawRect(background, paint)

                        val icon = ContextCompat.getDrawable(context, iconId)
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