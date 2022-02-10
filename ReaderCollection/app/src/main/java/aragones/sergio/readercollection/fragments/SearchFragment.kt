/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentSearchBinding
import aragones.sergio.readercollection.extensions.hideSoftKeyboard
import aragones.sergio.readercollection.utils.ScrollPosition
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import kotlin.math.max

class SearchFragment : BindingFragment<FragmentSearchBinding>(), OnItemClickListener {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: SearchViewModel
    private lateinit var booksAdapter: BooksAdapter
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.search_toolbar_menu, menu)
        setupSearchView(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onItemClick(bookId: String) {

        val action = SearchFragmentDirections.actionSearchFragmentToBookDetailFragment(bookId, true)
        findNavController().navigate(action)
    }

    override fun onLoadMoreItemsClick() {

        viewModel.searchBooks()
        viewModel.setPosition(ScrollPosition.MIDDLE)
    }

    override fun onShowAllItemsClick(state: String) {}
    //endregion

    //region Public methods
    fun goToStartEndList(view: View) {

        when (view) {
            binding.floatingActionButtonStartList -> {
                viewModel.setPosition(ScrollPosition.TOP)
                binding.recyclerViewBooks.scrollToPosition(0)
            }
            binding.floatingActionButtonEndList -> {
                viewModel.setPosition(ScrollPosition.END)
                binding.recyclerViewBooks.scrollToPosition(booksAdapter.itemCount - 1)
            }
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            SearchViewModelFactory(application)
        )[SearchViewModel::class.java]
        booksAdapter = BooksAdapter(
            books = viewModel.books.value ?: mutableListOf(),
            isVerticalDesign = false,
            isGoogleBook = true,
            onItemClickListener = this
        )
        setupBindings()

        with(binding) {

            swipeRefreshLayoutBooks.setOnRefreshListener {

                this@SearchFragment.viewModel.reloadData()
                this@SearchFragment.viewModel.searchBooks()
            }
            recyclerViewBooks.adapter = booksAdapter
            recyclerViewBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val position =
                        if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            ScrollPosition.TOP
                        } else if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                            ScrollPosition.END
                        } else {
                            ScrollPosition.MIDDLE
                        }
                    this@SearchFragment.viewModel.setPosition(position)
                }
            })
            ItemTouchHelper(SwipeController()).attachToRecyclerView(recyclerViewBooks)

            fragment = this@SearchFragment
            viewModel = this@SearchFragment.viewModel
            lifecycleOwner = this@SearchFragment
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner) { booksResponse ->

            if (booksResponse.isEmpty()) {
                booksAdapter.resetList()
            } else {
                booksAdapter.setBooks(booksResponse, false)
            }
        }

        viewModel.searchLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayoutBooks.isRefreshing = isLoading
        }

        viewModel.bookAdded.observe(viewLifecycleOwner) { position ->
            position?.let {

                val message = resources.getString(R.string.book_saved)
                showPopupDialog(message)
            }
        }

        viewModel.searchError.observe(viewLifecycleOwner) { error ->
            error?.let {

                manageError(it)
            }
        }
    }

    private fun setupSearchView(menu: Menu) {

        val menuItem = menu.findItem(R.id.action_search)
        this.searchView = menuItem.actionView as SearchView
        this.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                searchBooks(query)
                menuItem.collapseActionView()
                return true
            }
        })
        this.setupSearchView(R.color.colorPrimary, viewModel.query.value ?: "")
    }

    private fun searchBooks(query: String) {

        viewModel.setSearch(query)
        viewModel.reloadData()
        viewModel.searchBooks()
        requireActivity().hideSoftKeyboard()
    }
    //endregion

    //region SwipeController
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

                        val icon = ContextCompat.getDrawable(context, R.drawable.ic_save_book)
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
    //endregion
}