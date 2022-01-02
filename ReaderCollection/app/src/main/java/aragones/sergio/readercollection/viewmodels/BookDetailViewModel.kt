/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.GoogleBookRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookDetailViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val googleBookRepository: GoogleBookRepository,
    private val stateRepository: StateRepository
): BaseViewModel() {

    //region Private properties
    private var bookId: String = ""
    private var isGoogleBook: Boolean = false
    private val _book = MutableLiveData<BookResponse>()
    private val _isFavourite = MutableLiveData<Boolean>()
    private val _formats = MutableLiveData<List<FormatResponse>>()
    private val _states = MutableLiveData<List<StateResponse>>()
    private val _bookDetailLoading = MutableLiveData<Boolean>()
    private val _bookDetailFormatsLoading = MutableLiveData<Boolean>()
    private val _bookDetailStatesLoading = MutableLiveData<Boolean>()
    private val _bookDetailFavouriteLoading = MutableLiveData<Boolean>()
    private val _bookDetailSuccessMessage = MutableLiveData<Int>()
    private val _bookDetailError = MutableLiveData<ErrorResponse>()
    //endregion

    //region Public properties
    val book: LiveData<BookResponse> = _book
    val isFavourite: LiveData<Boolean> = _isFavourite
    val formats: LiveData<List<FormatResponse>> = _formats
    val states: LiveData<List<StateResponse>> = _states
    val bookDetailLoading: LiveData<Boolean> = _bookDetailLoading
    val bookDetailFormatsLoading: LiveData<Boolean> = _bookDetailFormatsLoading
    val bookDetailStatesLoading: LiveData<Boolean> = _bookDetailStatesLoading
    val bookDetailFavouriteLoading: LiveData<Boolean> = _bookDetailFavouriteLoading
    val bookDetailSuccessMessage: LiveData<Int> = _bookDetailSuccessMessage
    val bookDetailError: LiveData<ErrorResponse> = _bookDetailError
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun getBook() {

        _bookDetailLoading.value = true
        if (isGoogleBook) {

            googleBookRepository.getBookObserver(bookId).subscribeBy(
                onSuccess = {

                    _book.value = Constants.mapGoogleBook(it)
                    _bookDetailLoading.value = false
                },
                onError = {

                    _bookDetailLoading.value = false
                    _bookDetailError.value = ErrorResponse("", R.string.error_server)
                    onDestroy()
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

                    _bookDetailLoading.value = false
                    _bookDetailError.value = ErrorResponse("", R.string.error_no_book)
                    onDestroy()
                }
            ).addTo(disposables)
        }
    }

    fun getFormats() {

        _bookDetailFormatsLoading.value = true
        formatRepository.getFormatsDatabaseObserver().subscribeBy(
            onSuccess = {

                _formats.value = it
                _bookDetailFormatsLoading.value = false
            },
            onError = {

                _formats.value = ArrayList()
                _bookDetailFormatsLoading.value = false
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun getStates() {

        _bookDetailStatesLoading.value = true
        stateRepository.getStatesDatabaseObserver().subscribeBy(
            onSuccess = {

                _states.value = it
                _bookDetailStatesLoading.value = false
            },
            onError = {

                _states.value = ArrayList()
                _bookDetailStatesLoading.value = false
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun createBook(book: BookResponse) {

        _bookDetailLoading.value = true
        booksRepository.createBookObserver(book).subscribeBy(
            onComplete = {

                _bookDetailLoading.value = false
                _bookDetailSuccessMessage.value = R.string.book_saved
            },
            onError = {

                _bookDetailLoading.value = false
                _bookDetailError.value = ApiManager.handleError(it)
                onDestroy()
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

                _bookDetailLoading.value = false
                _bookDetailError.value = ApiManager.handleError(it)
                onDestroy()
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

                _bookDetailLoading.value = false
                _bookDetailError.value = ApiManager.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
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
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun setBookId(bookId: String) {
        this.bookId = bookId
    }

    fun setIsGoogleBook(isGoogleBook: Boolean) {
        this.isGoogleBook = isGoogleBook
    }
    //endregion
}