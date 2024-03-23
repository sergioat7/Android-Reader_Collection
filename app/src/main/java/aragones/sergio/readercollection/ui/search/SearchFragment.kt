/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.search

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentSearchBinding
import aragones.sergio.readercollection.extensions.hideSoftKeyboard
import aragones.sergio.readercollection.extensions.style
import aragones.sergio.readercollection.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.interfaces.OnItemClickListener
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.books.BooksAdapter
import aragones.sergio.readercollection.ui.components.InformationAlertDialog
import com.aragones.sergio.util.ScrollPosition
import com.aragones.sergio.util.StatusBarStyle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class SearchFragment :
    BindingFragment<FragmentSearchBinding>(),
    MenuProviderInterface,
    OnItemClickListener {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: SearchViewModel by viewModels()
    private var booksAdapter: BooksAdapter? = null
    private val lastItemPosition: Int get() = (booksAdapter?.itemCount ?: 1) - 1
    private var toolbarSequence: TapTargetSequence? = null
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
        binding.composeView.setContent {

            val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)
            val text = if (infoDialogMessageId != -1) {
                getString(infoDialogMessageId)
            } else {
                ""
            }
            InformationAlertDialog(show = infoDialogMessageId != -1, text = text) {
                viewModel.closeDialogs()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        /*
        Must be created with a delay in order to wait for the fragment menu creation,
        otherwise it wouldn't be icons in the toolbar
         */
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            createSequence()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()
    }

    override fun onStop() {
        super.onStop()

        toolbarSequence?.cancel()
        toolbarSequence = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        booksAdapter = null
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menu.clear()
        menuInflater.inflate(R.menu.search_toolbar_menu, menu)
        setupSearchView(menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

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
                binding.recyclerViewBooks.scrollToPosition(lastItemPosition)
            }
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

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
                booksAdapter?.resetList()
            } else {
                booksAdapter?.setBooks(booksResponse, false)
            }
        }

        viewModel.searchLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayoutBooks.isRefreshing = isLoading
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

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            toolbarSequence = TapTargetSequence(requireActivity()).apply {
                target(
                    TapTarget.forToolbarMenuItem(
                        binding.toolbar,
                        binding.toolbar.menu.findItem(R.id.action_search).itemId,
                        resources.getString(R.string.search_bar_tutorial_title),
                        resources.getString(R.string.search_bar_tutorial_description)
                    ).style(requireContext()).cancelable(true).tintTarget(true)
                )
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        viewModel.setTutorialAsShown()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                start()
            }
        }
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
            booksAdapter?.notifyItemChanged(position)
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