/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.databinding.FragmentBooksBinding
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.SortingPickerAlertDialog
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.StatusBarStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BooksFragment : BindingFragment<FragmentBooksBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: BooksViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchBooks()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ReaderCollectionTheme {
                val state by viewModel.state
                val sortingPickerState by viewModel.sortingPickerState
                val error by viewModel.booksError.observeAsState()

                BooksScreen(
                    state = state,
                    onSortClick = {
                        viewModel.showSortingPickerState()
                    },
                    onSearch = {
                        viewModel.searchBooks(it)
                    },
                    onBookClick = { bookId ->
                        val action = BooksFragmentDirections
                            .actionBooksFragmentToBookDetailFragment(
                                bookId,
                                false,
                            )
                        findNavController().navigate(action)
                    },
                    onShowAll = { bookState ->
                        val action = BooksFragmentDirections
                            .actionBooksFragmentToBookListFragment(
                                bookState,
                                sortingPickerState.sortParam,
                                sortingPickerState.isSortDescending,
                                state.query,
                            )
                        findNavController().navigate(action)
                    },
                    onSwitchToLeft = { fromIndex ->
                        viewModel.switchBooksPriority(fromIndex, fromIndex - 1)
                    },
                    onSwitchToRight = { fromIndex ->
                        viewModel.switchBooksPriority(fromIndex, fromIndex + 1)
                    },
                    onBookStateChange = viewModel::setBook,
                    onAddBook = {
                    },
                )

                SortingPickerAlertDialog(
                    state = sortingPickerState,
                    onCancel = {
                        viewModel.updatePickerState(
                            sortingPickerState.sortParam,
                            sortingPickerState.isSortDescending,
                        )
                    },
                    onAccept = { newSortParam, newIsSortDescending ->
                        viewModel.updatePickerState(newSortParam, newIsSortDescending)
                    },
                )

                val text = if (error != null) {
                    val errorText = StringBuilder()
                    if (requireNotNull(error).error.isNotEmpty()) {
                        errorText.append(requireNotNull(error).error)
                    } else {
                        errorText.append(resources.getString(requireNotNull(error).errorKey))
                    }
                    errorText.toString()
                } else {
                    ""
                }
                InformationAlertDialog(show = text.isNotEmpty(), text = text) {
                    viewModel.closeDialogs()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion
}