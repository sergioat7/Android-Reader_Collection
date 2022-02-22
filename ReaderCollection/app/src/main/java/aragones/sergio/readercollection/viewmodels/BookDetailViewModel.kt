/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.GoogleBookRepository
import aragones.sergio.readercollection.repositories.UserRepository
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

    fun createBook(book: BookResponse) {

        _bookDetailLoading.value = true
        booksRepository.createBookObserver(book).subscribeBy(
            onComplete = {

                _bookDetailLoading.value = false
                _bookDetailSuccessMessage.value = R.string.book_saved
            },
            onError = {
                manageError(ApiManager.handleError(it))
            }
        ).addTo(disposables)
    }

    fun setBook(book: BookResponse) {

        _bookDetailLoading.value = true
        booksRepository.updateBookObserver(book).subscribeBy(
            onSuccess = {

                _book.value = it
                _bookDetailLoading.value = false
            },
            onError = {
                manageError(ApiManager.handleError(it))
            }
        ).addTo(disposables)
    }

    fun deleteBook() {

        _bookDetailLoading.value = true
        booksRepository.deleteBookObserver(bookId).subscribeBy(
            onComplete = {

                _bookDetailLoading.value = false
                _bookDetailSuccessMessage.value = R.string.book_removed
            },
            onError = {
                manageError(ApiManager.handleError(it))
            }
        ).addTo(disposables)
    }

    fun setBookImage(imageUri: String?) {
        _bookImage.value = imageUri
    }

    fun setFavourite(isFavourite: Boolean) {

        _bookDetailFavouriteLoading.value = true
        booksRepository.setFavouriteBookObserver(bookId, isFavourite).subscribeBy(
            onSuccess = {

                _isFavourite.value = it.isFavourite
                _bookDetailFavouriteLoading.value = false
            },
            onError = {
                _bookDetailFavouriteLoading.value = false
            }
        ).addTo(disposables)
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