/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.presentation.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.navigation.Route
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
) : ViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.BookDetail>()
    private lateinit var currentBook: Book
    private lateinit var savedBooks: List<Book>
    private var _state: MutableStateFlow<BookDetailUiState> = MutableStateFlow(
        BookDetailUiState(
            book = null,
            isAlreadySaved = true,
            isEditable = false,
        ),
    )
    private val _bookDetailError = MutableStateFlow<ErrorModel?>(null)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _imageDialogMessageId = MutableStateFlow(-1)
    private val pendingBooks: List<Book>
        get() = savedBooks.filter { it.isPending() }
    //endregion

    //region Public properties
    val state: StateFlow<BookDetailUiState> = _state
    val bookDetailError: StateFlow<ErrorModel?> = _bookDetailError
    var confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    var imageDialogMessageId: StateFlow<Int> = _imageDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onCreate() {
        viewModelScope.launch {
            booksRepository.getBooks().collect {
                savedBooks = it
            }
        }
        fetchBook()
    }
    //endregion

    //region Public methods
    fun enableEdition() {
        _state.update { it.copy(isEditable = true) }
    }

    fun disableEdition() {
        _state.update {
            it.copy(
                book = currentBook,
                isEditable = false,
            )
        }
    }

    fun changeData(book: Book) {
        _state.update { it.copy(book = book) }
    }

    fun createBook(newBook: Book) = viewModelScope.launch {
        val maxPriority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1)
        val book = newBook.copy(priority = maxPriority + 1)
        booksRepository.createBook(book).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.book_saved
                _state.update {
                    it.copy(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    )
                }
            },
            onFailure = {
                _bookDetailError.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                )
            },
        )
    }

    fun setBook(book: Book) = viewModelScope.launch {
        booksRepository.setBook(book).fold(
            onSuccess = { updatedBook ->
                currentBook = updatedBook
                _state.update {
                    it.copy(
                        book = updatedBook,
                        isEditable = false,
                    )
                }
            },
            onFailure = {
                _bookDetailError.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                )
            },
        )
    }

    fun deleteBook() = viewModelScope.launch {
        booksRepository.deleteBook(params.bookId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.book_removed
                _state.update {
                    it.copy(
                        isAlreadySaved = false,
                        isEditable = true,
                    )
                }
            },
            onFailure = {
                _bookDetailError.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                )
            },
        )
    }

    fun setBookImage(imageUri: String?) {
        _state.update {
            it.copy(
                book = _state.value.book?.copy(
                    thumbnail = imageUri,
                ),
            )
        }
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
    private fun fetchBook() = viewModelScope.launch {
        if (params.friendId.isNotEmpty()) {
            booksRepository.getFriendBook(params.friendId, params.bookId).fold(
                onSuccess = { book ->
                    currentBook = book
                    _state.update {
                        it.copy(
                            book = book,
                            isEditable = true,
                            isAlreadySaved = false,
                        )
                    }
                },
                onFailure = {
                    _bookDetailError.value = ErrorModel("", R.string.error_no_book)
                },
            )
        } else {
            booksRepository.getBook(params.bookId).fold(
                onSuccess = { (book, isAlreadySaved) ->
                    currentBook = book
                    _state.update {
                        it.copy(
                            book = book,
                            isEditable = !isAlreadySaved,
                            isAlreadySaved = isAlreadySaved,
                        )
                    }
                },
                onFailure = {
                    _bookDetailError.value = ErrorModel("", R.string.error_no_book)
                },
            )
        }
    }
    //endregion
}