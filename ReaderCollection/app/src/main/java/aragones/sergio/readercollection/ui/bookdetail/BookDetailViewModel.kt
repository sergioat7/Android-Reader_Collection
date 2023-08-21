/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.ui.bookdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.GoogleBookRepository
import aragones.sergio.readercollection.data.source.UserRepository
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookDetailViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val googleBookRepository: GoogleBookRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private var bookId: String = ""
    private val _book = MutableLiveData<BookResponse>()
    private val _bookImage = MutableLiveData<String?>()
    private val _isFavourite = MutableLiveData<Boolean>()
    private val _bookDetailLoading = MutableLiveData<Boolean>()
    private val _bookDetailFormatsLoading = MutableLiveData<Boolean>()
    private val _bookDetailStatesLoading = MutableLiveData<Boolean>()
    private val _bookDetailFavouriteLoading = MutableLiveData<Boolean>()
    private val _bookDetailSuccessMessage = MutableLiveData<Int>()
    private val _bookDetailError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    var isGoogleBook: Boolean = false
    val book: LiveData<BookResponse> = _book
    val bookImage: LiveData<String?> = _bookImage
    val isFavourite: LiveData<Boolean> = _isFavourite
    val bookDetailLoading: LiveData<Boolean> = _bookDetailLoading
    val bookDetailFormatsLoading: LiveData<Boolean> = _bookDetailFormatsLoading
    val bookDetailStatesLoading: LiveData<Boolean> = _bookDetailStatesLoading
    val bookDetailFavouriteLoading: LiveData<Boolean> = _bookDetailFavouriteLoading
    val bookDetailSuccessMessage: LiveData<Int> = _bookDetailSuccessMessage
    val bookDetailError: LiveData<ErrorResponse?> = _bookDetailError
    var newBookTutorialShown = userRepository.hasNewBookTutorialBeenShown
    var bookDetailsTutorialShown = userRepository.hasBookDetailsTutorialBeenShown
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        googleBookRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBook() {

        _bookDetailLoading.value = true
        if (isGoogleBook) {

            googleBookRepository.getBookObserver(bookId).subscribeBy(
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

    fun createBook(newBook: BookResponse) {

        _bookDetailLoading.value = true
        booksRepository.createBook(newBook, success = {

            _bookDetailLoading.value = false
            _bookDetailSuccessMessage.value = R.string.book_saved
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
            _bookDetailSuccessMessage.value = R.string.book_removed
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

    fun setBookId(bookId: String) {
        this.bookId = bookId
    }

    fun setIsGoogleBook(isGoogleBook: Boolean) {
        this.isGoogleBook = isGoogleBook
    }

    fun setNewBookTutorialAsShown() {
        userRepository.setHasNewBookTutorialBeenShown(true)
        newBookTutorialShown = true
    }

    fun setBookDetailsTutorialAsShown() {
        userRepository.setHasBookDetailsTutorialBeenShown(true)
        bookDetailsTutorialShown = true
    }
    //endregion

    //region Private methods
    private fun manageError(error: ErrorResponse) {

        _bookDetailLoading.value = false
        _bookDetailError.value = error
        _bookDetailError.value = null
    }
    //endregion
}