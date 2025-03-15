/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.presentation.bookdetail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.navigation.Route
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
) : BaseViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.BookDetail>()
    private lateinit var currentBook: Book
    private lateinit var savedBooks: List<Book>
    private var _state: MutableState<BookDetailUiState> = mutableStateOf(
        BookDetailUiState(
            book = null,
            isAlreadySaved = !params.isGoogleBook,
            isEditable = false,
        ),
    )
    private val _bookDetailError = MutableStateFlow<ErrorResponse?>(null)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _imageDialogMessageId = MutableStateFlow(-1)
    private val pendingBooks: List<Book>
        get() = savedBooks.filter { it.isPending() }
    //endregion

    //region Public properties
    val state: State<BookDetailUiState> = _state
    val bookDetailError: StateFlow<ErrorResponse?> = _bookDetailError
    var confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    var imageDialogMessageId: StateFlow<Int> = _imageDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onCreate() {
        booksRepository
            .getBooks()
            .subscribeBy(
                onComplete = {
                    savedBooks = listOf()
                },
                onNext = {
                    savedBooks = it
                },
                onError = {
                    savedBooks = listOf()
                },
            ).addTo(disposables)
        fetchBook()
    }

    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun enableEdition() {
        _state.value = _state.value.copy(
            isEditable = true,
        )
    }

    fun disableEdition() {
        _state.value = _state.value.copy(
            book = currentBook,
            isEditable = false,
        )
    }

    fun changeData(book: Book) {
        _state.value = _state.value.copy(
            book = book,
        )
    }

    fun createBook(newBook: Book) {
        if (savedBooks.firstOrNull { it.id == newBook.id } != null) {
            _infoDialogMessageId.value = R.string.error_resource_found
            return
        }
        newBook.priority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1) + 1
        booksRepository
            .createBook(newBook)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.book_saved
                },
                onError = {
                    _bookDetailError.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database,
                    )
                },
            ).addTo(disposables)
    }

    fun setBook(book: Book) {
        booksRepository
            .setBook(book)
            .subscribeBy(
                onSuccess = {
                    currentBook = it
                    _state.value = _state.value.copy(
                        book = it,
                        isEditable = false,
                    )
                },
                onError = {
                    _bookDetailError.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database,
                    )
                },
            ).addTo(disposables)
    }

    fun deleteBook() {
        booksRepository
            .deleteBook(params.bookId)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.book_removed
                },
                onError = {
                    _bookDetailError.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database,
                    )
                },
            ).addTo(disposables)
    }

    fun setBookImage(imageUri: String?) {
        _state.value = _state.value.copy(
            book = _state.value.book?.copy(
                thumbnail = imageUri,
            ),
        )
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun showImageDialog(textId: Int) {
        _imageDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
        _imageDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun fetchBook() {
        if (!_state.value.isAlreadySaved) {
            booksRepository
                .getRemoteBook(params.bookId)
                .subscribeBy(
                    onSuccess = {
                        currentBook = it
                        _state.value = _state.value.copy(
                            book = it,
                            isEditable = true,
                        )
                    },
                    onError = {
                        _bookDetailError.value = ErrorResponse("", R.string.error_server)
                    },
                ).addTo(disposables)
        } else {
            booksRepository
                .getBook(params.bookId)
                .subscribeBy(
                    onSuccess = {
                        currentBook = it
                        _state.value = _state.value.copy(
                            book = it,
                            isEditable = false,
                        )
                    },
                    onError = {
                        _bookDetailError.value = ErrorResponse("", R.string.error_no_book)
                    },
                ).addTo(disposables)
        }
    }
    //endregion
}