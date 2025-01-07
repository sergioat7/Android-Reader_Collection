/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.booklist

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
import aragones.sergio.readercollection.databinding.FragmentBookListBinding
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.SortingPickerAlertDialog
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.StatusBarStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookListFragment : BindingFragment<FragmentBookListBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: BookListViewModel by viewModels()
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
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ReaderCollectionTheme {
                    val state by viewModel.uiState
                    val sortingPickerState by viewModel.sortingPickerState
                    val error by viewModel.booksError.observeAsState()

                    when (val currentState = state) {
                        is BookListUiState.Success -> {
                            if (currentState.books.isEmpty() && !currentState.isLoading) {
                                findNavController().popBackStack()
                                return@ReaderCollectionTheme
                            }
                        }
                        else -> {
                            Unit
                        }
                    }

                    BookListScreen(
                        state = state,
                        onBookClick = { bookId ->
                            val action =
                                BookListFragmentDirections
                                    .actionBookListFragmentToBookDetailFragment(
                                        bookId,
                                        false,
                                    )
                            findNavController().navigate(action)
                        },
                        onBack = { findNavController().popBackStack() },
                        onDragClick = {
                            viewModel.switchDraggingState()
                        },
                        onSortClick = {
                            viewModel.showSortingPickerState()
                        },
                        onDrag = {
                            viewModel.updateBookOrdering(it)
                        },
                        onDragEnd = {
                            viewModel.setPriorityFor(it)
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
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion
}