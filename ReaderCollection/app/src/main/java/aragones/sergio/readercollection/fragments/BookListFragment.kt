/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2022
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.BooksAdapter
import aragones.sergio.readercollection.adapters.OnItemClickListener
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentBookListBinding
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.utils.ScrollPosition
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.BookListViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class BookListFragment : BindingFragment<FragmentBookListBinding>(), OnItemClickListener {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val args: BookListFragmentArgs by navArgs()
    private lateinit var viewModel: BookListViewModel
    private lateinit var booksAdapter: BooksAdapter
    private val goBack = MutableLiveData<Boolean>()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onResume() {
        super.onResume()

        if (this::viewModel.isInitialized) viewModel.fetchBooks()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onItemClick(bookId: String) {

        val action =
            BookListFragmentDirections.actionBookListFragmentToBookDetailFragment(bookId, false)
        findNavController().navigate(action)
    }

    override fun onLoadMoreItemsClick() {}

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
            BookListViewModelFactory(
                application,
                args.state,
                args.sortParam,
                args.isSortDescending,
                args.query,
                args.month,
                args.author,
                args.format
            )
        )[BookListViewModel::class.java]
        booksAdapter = BooksAdapter(
            books = viewModel.books.value?.toMutableList() ?: mutableListOf(),
            isVerticalDesign = false,
            isGoogleBook = false,
            onItemClickListener = this
        )
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
    //endregion
}