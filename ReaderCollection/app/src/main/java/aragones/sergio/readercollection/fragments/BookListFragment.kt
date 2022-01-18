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
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentBookListBinding
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.BookListViewModelFactory
import aragones.sergio.readercollection.viewmodels.BookListViewModel

class BookListFragment : BindingFragment<FragmentBookListBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val args: BookListFragmentArgs by navArgs()
    private lateinit var viewModel: BookListViewModel
    private val goBack = MutableLiveData<Boolean>()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized) viewModel.onDestroy()
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
                args.query
            )
        )[BookListViewModel::class.java]
        setupBindings()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.isDarkMode = context?.isDarkMode()
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.books.observe(viewLifecycleOwner, {
            //TODO: show books
        })

        viewModel.booksLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.booksError.observe(viewLifecycleOwner, {
            manageError(it, goBack)
        })

        goBack.observe(viewLifecycleOwner, {
            findNavController().popBackStack()
        })
    }
    //endregion
}