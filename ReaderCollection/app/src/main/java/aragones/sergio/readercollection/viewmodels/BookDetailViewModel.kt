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
    private val _bookDetailLoading = MutableLiveData<Boolean>()
    private val _bookDetailError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val book: LiveData<BookResponse> = _book
    val bookDetailLoading: LiveData<Boolean> = _bookDetailLoading
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

    fun setBookId(bookId: String) {
        this.bookId = bookId
    }

    fun setIsGoogleBook(isGoogleBook: Boolean) {
        this.isGoogleBook = isGoogleBook
    }
}