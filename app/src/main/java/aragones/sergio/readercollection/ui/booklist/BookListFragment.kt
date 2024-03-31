/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.booklist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentBookListBinding
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.extensions.style
import aragones.sergio.readercollection.presentation.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener
import aragones.sergio.readercollection.presentation.interfaces.OnStartDraggingListener
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.books.BooksAdapter
import aragones.sergio.readercollection.ui.books.BooksViewHolder
import com.aragones.sergio.util.ScrollPosition
import com.aragones.sergio.util.StatusBarStyle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookListFragment :
    BindingFragment<FragmentBookListBinding>(),
    MenuProviderInterface,
    OnItemClickListener,
    OnStartDraggingListener {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: BookListViewModel by viewModels()
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var touchHelper: ItemTouchHelper
    private val goBack = MutableLiveData<Boolean>()
    private lateinit var menu: Menu
    private var toolbarSequence: TapTargetSequence? = null
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onStart() {
        super.onStart()

        if (viewModel.arePendingBooks) {
            /*
        Must be created with a delay in order to wait for the fragment menu creation,
        otherwise it wouldn't be icons in the toolbar
         */
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                createSequence()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchBooks()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()

        toolbarSequence?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        this.menu = menu
        menu.clear()

        menuInflater.inflate(R.menu.book_list_toolbar_menu, menu)
        menu.findItem(R.id.action_enable_drag).isVisible =
            viewModel.arePendingBooks && viewModel.query.isEmpty()
        menu.findItem(R.id.action_disable_drag).isVisible = false
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return when (menuItem.itemId) {
            R.id.action_enable_drag -> {

                menu.findItem(R.id.action_enable_drag).isVisible = false
                menu.findItem(R.id.action_disable_drag).isVisible = true
                booksAdapter.setDragging(true)
                true
            }

            R.id.action_disable_drag -> {

                menu.findItem(R.id.action_enable_drag).isVisible = true
                menu.findItem(R.id.action_disable_drag).isVisible = false
                booksAdapter.setDragging(false)
                true
            }

            R.id.action_sort -> {

                viewModel.sort(requireContext()) {
                    viewModel.setPosition(ScrollPosition.TOP)
                    binding.recyclerViewBooks.scrollToPosition(0)
                }
                true
            }

            else -> false
        }
    }

    override fun onItemClick(bookId: String) {

        val action =
            BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(bookId, false)
        findNavController().navigate(action)
    }

    override fun onLoadMoreItemsClick() {}

    override fun onShowAllItemsClick(state: String) {}

    override fun onStartDragging(viewHolder: BooksViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    override fun onFinishDragging(books: List<Book>) {
        viewModel.setPriorityFor(books)
    }
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

        booksAdapter = BooksAdapter(
            books = viewModel.books.value?.toMutableList() ?: mutableListOf(),
            isVerticalDesign = false,
            isGoogleBook = false,
            onItemClickListener = this,
            onStartDraggingListener = this
        )
        touchHelper = ItemTouchHelper(ItemMoveCallback(booksAdapter))
        setupBindings()

        binding.recyclerViewBooks.apply {
            adapter = booksAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
                    viewModel.setPosition(position)
                }
            })
            touchHelper.attachToRecyclerView(this)
        }

        binding.fragment = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.isDarkMode = context?.isDarkMode()
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner) {
            booksAdapter.setBooks(it.toMutableList(), true)
        }

        viewModel.booksLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.booksError.observe(viewLifecycleOwner) {
            manageError(it, goBack)
        }

        goBack.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            toolbarSequence = TapTargetSequence(requireActivity()).apply {
                target(
                    TapTarget.forToolbarMenuItem(
                        binding.toolbar,
                        binding.toolbar.menu.findItem(R.id.action_enable_drag).itemId,
                        resources.getString(R.string.drag_bar_tutorial_title),
                        resources.getString(R.string.drag_bar_tutorial_description)
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
}