/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.repositories.BookDetailRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookDetailViewModel @Inject constructor(
    private val bookDetailRepository: BookDetailRepository
): ViewModel() {

    //MARK: - Private properties

    private var bookId: String = ""
    private var isGoogleBook: Boolean = false
    private val _book = MutableLiveData<BookResponse>()
    private val _formats = MutableLiveData<List<FormatResponse>>()
    private val _states = MutableLiveData<List<StateResponse>>()
    private val _bookDetailLoading = MutableLiveData<Boolean>()
    private val _bookDetailFormatsLoading = MutableLiveData<Boolean>()
    private val _bookDetailStatesLoading = MutableLiveData<Boolean>()
    private val _bookDetailError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val book: LiveData<BookResponse> = _book
    val formats: LiveData<List<FormatResponse>> = _formats
    val states: LiveData<List<StateResponse>> = _states
    val bookDetailLoading: LiveData<Boolean> = _bookDetailLoading
    val bookDetailFormatsLoading: LiveData<Boolean> = _bookDetailFormatsLoading
    val bookDetailStatesLoading: LiveData<Boolean> = _bookDetailStatesLoading
    val bookDetailError: LiveData<ErrorResponse> = _bookDetailError

    //MARK: - Public methods

    fun getBook() {

        _bookDetailLoading.value = true
        if (isGoogleBook) {

            bookDetailRepository.getGoogleBook(bookId).subscribeBy(
                onSuccess = {

                    _book.value = Constants.mapGoogleBook(it)
                    _bookDetailLoading.value = false
                },
                onError = {

                    _bookDetailLoading.value = false
                    _bookDetailError.value = ErrorResponse("", R.string.error_server)
                }
            )
        } else {

            bookDetailRepository.getBook(bookId).subscribeBy(
                onComplete = {

                    _bookDetailLoading.value = false
                    _bookDetailError.value = ErrorResponse("", R.string.error_no_book)
                },
                onSuccess = {

                    _book.value = it
                    _bookDetailLoading.value = false
                },
                onError = {

                    _bookDetailLoading.value = false
                    _bookDetailError.value = Constants.handleError(it)
                }
            )
        }
    }

    fun getFormats() {

        _bookDetailFormatsLoading.value = true
        bookDetailRepository.getFormats().subscribeBy(
            onSuccess = {

                _formats.value = it
                _bookDetailFormatsLoading.value = false
            },
            onError = {

                _formats.value = ArrayList()
                _bookDetailFormatsLoading.value = false
            }
        )
    }

    fun getStates() {

        _bookDetailStatesLoading.value = true
        bookDetailRepository.getStates().subscribeBy(
            onSuccess = {

                _states.value = it
                _bookDetailStatesLoading.value = false
            },
            onError = {

                _states.value = ArrayList()
                _bookDetailStatesLoading.value = false
            }
        )
    }

    fun setBookId(bookId: String) {
        this.bookId = bookId
    }

    fun setIsGoogleBook(isGoogleBook: Boolean) {
        this.isGoogleBook = isGoogleBook
    }
}