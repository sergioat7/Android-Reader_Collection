/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.search

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentSearchBinding
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.StatusBarStyle
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BindingFragment<FragmentSearchBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: SearchViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            ReaderCollectionTheme {
                val state by viewModel.state

                SearchScreen(
                    state = state,
                    onBookClick = { bookId ->
                        val action = SearchFragmentDirections
                            .actionSearchFragmentToBookDetailFragment(
                                bookId,
                                true,
                            )
                        findNavController().navigate(action)
                    },
                    onSwipe = viewModel::addBook,
                    onSearch = {
                        viewModel.searchBooks(reload = true, query = it)
                    },
                    onLoadMoreClick = viewModel::searchBooks,
                    onRefresh = {
                        viewModel.searchBooks(reload = true)
                    },
                    onBack = { findNavController().popBackStack() },
                )

                val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(
                    initial = -1,
                )

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
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion
}