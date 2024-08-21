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
import aragones.sergio.readercollection.databinding.FragmentSearchBinding
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import com.aragones.sergio.util.StatusBarStyle
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

            val books by viewModel.books.observeAsState()
            val query by viewModel.query.observeAsState()
            val searchLoading by viewModel.searchLoading.observeAsState()

            SearchScreen(
                books = books?.toList() ?: listOf(),
                isLoading = searchLoading ?: false,
                query = query?.ifBlank { null },
                onBookClick = { bookId ->
                    val action = SearchFragmentDirections.actionSearchFragmentToBookDetailFragment(
                        bookId,
                        true
                    )
                    findNavController().navigate(action)
                },
                onSearch = this::searchBooks,
                onLoadMoreClick = viewModel::searchBooks,
                onRefresh = {
                    viewModel.reloadData()
                    viewModel.searchBooks()
                },
            )

            val error by viewModel.searchError.observeAsState()
            val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)

            val text = if (error != null) {
                val errorText = StringBuilder()
                if (requireNotNull(error).error.isNotEmpty()) {
                    errorText.append(requireNotNull(error).error)
                } else {
                    errorText.append(resources.getString(requireNotNull(error).errorKey))
                }
                errorText.toString()
            } else if (infoDialogMessageId != -1) {
                getString(infoDialogMessageId)
            } else {
                ""
            }
            InformationAlertDialog(show = infoDialogMessageId != -1, text = text) {
                viewModel.closeDialogs()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun searchBooks(query: String) {

        viewModel.setSearch(query)
        viewModel.reloadData()
        viewModel.searchBooks()
    }
    //endregion
}