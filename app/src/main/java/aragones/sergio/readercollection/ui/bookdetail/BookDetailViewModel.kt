/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.ui.bookdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.ui.base.BaseViewModel
import com.aragones.sergio.data.business.BookResponse
import com.aragones.sergio.data.business.ErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val bookId: String =
        state["bookId"] ?: throw IllegalStateException("Book id not found in the state handle")
    private val _book = MutableLiveData<BookResponse>()
    private val _bookImage = MutableLiveData<String?>()
    private val _isFavourite = MutableLiveData<Boolean>()
    private val _bookDetailLoading = MutableLiveData<Boolean>()
    private val _bookDetailFormatsLoading = MutableLiveData<Boolean>()
    private val _bookDetailStatesLoading = MutableLiveData<Boolean>()
    private val _bookDetailFavouriteLoading = MutableLiveData<Boolean>()
    private val _bookDetailError = MutableLiveData<ErrorResponse?>()
    private lateinit var pendingBooks: List<BookResponse>
    private val _confirmationDialogMessageId = MutableLiveData(-1)
    private val _infoDialogMessageId = MutableLiveData(-1)
    private val _imageDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    var isGoogleBook: Boolean = state["isGoogleBook"] ?: false
    val book: LiveData<BookResponse> = _book
    val bookImage: LiveData<String?> = _bookImage
    val isFavourite: LiveData<Boolean> = _isFavourite
    val bookDetailLoading: LiveData<Boolean> = _bookDetailLoading
    val bookDetailFormatsLoading: LiveData<Boolean> = _bookDetailFormatsLoading
    val bookDetailStatesLoading: LiveData<Boolean> = _bookDetailStatesLoading
    val bookDetailFavouriteLoading: LiveData<Boolean> = _bookDetailFavouriteLoading
    val bookDetailError: LiveData<ErrorResponse?> = _bookDetailError
    var newBookTutorialShown = userRepository.hasNewBookTutorialBeenShown
    var bookDetailsTutorialShown = userRepository.hasBookDetailsTutorialBeenShown
    var confirmationDialogMessageId: LiveData<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    var imageDialogMessageId: LiveData<Int> = _imageDialogMessageId
    //endregion

    //region Lifecycle methods
    init {

        booksRepository.getPendingBooksDatabaseObserver().subscribeBy(
            onComplete = {
                pendingBooks = listOf()
            },
            onSuccess = {
                pendingBooks = it
            },
            onError = {
                pendingBooks = listOf()
            }
        ).addTo(disposables)
        fetchBook()
    }

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun createBook(newBook: BookResponse) {

        newBook.priority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1) + 1
        _bookDetailLoading.value = true
        booksRepository.createBook(newBook, success = {

            _bookDetailLoading.value = false
            _infoDialogMessageId.value = R.string.book_saved
        }, failure = {
            manageError(it)
        })
    }

    fun setBook(book: BookResponse) {

        _bookDetailLoading.value = true
        booksRepository.setBook(book, success = {

            _book.value = it
            _bookDetailLoading.value = false
        }, failure = {
            manageError(it)
        })
    }

    fun deleteBook() {

        _bookDetailLoading.value = true
        booksRepository.deleteBook(bookId, success = {

            _bookDetailLoading.value = false
            _infoDialogMessageId.value = R.string.book_removed
        }, failure = {
            manageError(it)
        })
    }

    fun setBookImage(imageUri: String?) {
        _bookImage.value = imageUri
    }

    fun setFavourite(isFavourite: Boolean) {

        _bookDetailFavouriteLoading.value = true
        booksRepository.setFavouriteBook(bookId, isFavourite, success = {

            _isFavourite.value = it.isFavourite
            _bookDetailFavouriteLoading.value = false
        }, failure = {
            _bookDetailFavouriteLoading.value = false
        })
    }

    fun setNewBookTutorialAsShown() {
        userRepository.setHasNewBookTutorialBeenShown(true)
        newBookTutorialShown = true
    }

    fun setBookDetailsTutorialAsShown() {
        userRepository.setHasBookDetailsTutorialBeenShown(true)
        bookDetailsTutorialShown = true
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

        _bookDetailLoading.value = true
        if (isGoogleBook) {

            booksRepository.getBookObserver(bookId).subscribeBy(
                onSuccess = {

                    _book.value = BookResponse(it)
                    _bookDetailLoading.value = false
                },
                onError = {
                    manageError(ErrorResponse("", R.string.error_server))
                }
            ).addTo(disposables)
        } else {

            booksRepository.getBookDatabaseObserver(bookId).subscribeBy(
                onSuccess = {

                    _book.value = it
                    _isFavourite.value = it.isFavourite
                    _bookDetailLoading.value = false
                },
                onError = {
                    manageError(ErrorResponse("", R.string.error_no_book))
                }
            ).addTo(disposables)
        }
    }

    private fun manageError(error: ErrorResponse) {

        _bookDetailLoading.value = false
        _bookDetailError.value = error
        _bookDetailError.value = null
    }
    //endregion
}