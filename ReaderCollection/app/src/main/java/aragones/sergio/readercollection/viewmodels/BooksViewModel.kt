/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): ViewModel() {

    //MARK: - Private properties

    private val _books = MutableLiveData<MutableList<BookResponse>>()
    private val _formats = MutableLiveData<List<FormatResponse>>()
    private val _states = MutableLiveData<List<StateResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksFormatsLoading = MutableLiveData<Boolean>()
    private val _booksStatesLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private var _selectedFormat = MutableLiveData<String?>()
    private var _selectedState = MutableLiveData<String?>()
    private var _isFavourite = MutableLiveData<Boolean?>()

    //MARK: - Public properties

    val books: LiveData<MutableList<BookResponse>> = _books
    val formats: LiveData<List<FormatResponse>> = _formats
    val states: LiveData<List<StateResponse>> = _states
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksFormatsLoading: LiveData<Boolean> = _booksFormatsLoading
    val booksStatesLoading: LiveData<Boolean> = _booksStatesLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val selectedFormat: LiveData<String?> = _selectedFormat
    val selectedState: LiveData<String?> = _selectedState
    val isFavourite: LiveData<Boolean?> = _isFavourite

    //MARK: - Public methods

    fun getBooks() {

        _booksLoading.value = true
        booksRepository.getBooks(_selectedFormat.value, _selectedState.value, _isFavourite.value).subscribeBy(
            onComplete = {

                _books.value = mutableListOf()
                _booksLoading.value = false
            },
            onSuccess = {

                _books.value = it.toMutableList()
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = Constants.handleError(it)
            }
        )
    }

    fun getFormats() {

        _booksFormatsLoading.value = true
        booksRepository.getFormats().subscribeBy(
            onSuccess = {

                _formats.value = it
                _booksFormatsLoading.value = false
            },
            onError = {

                _formats.value = ArrayList()
                _booksFormatsLoading.value = false
            }
        )
    }

    fun getStates() {

        _booksStatesLoading.value = true
        booksRepository.getStates().subscribeBy(
            onSuccess = {

                _states.value = it
                _booksStatesLoading.value = false
            },
            onError = {

                _states.value = ArrayList()
                _booksStatesLoading.value = false
            }
        )
    }

    fun reloadData() {
        _books.value = mutableListOf()
    }

    fun setFormat(format: String?) {
        _selectedFormat.value = format
    }

    fun setState(state: String?) {
        _selectedState.value = state
    }

    fun setFavourite(isFavourite: Boolean?) {
        _isFavourite.value = isFavourite
    }
}