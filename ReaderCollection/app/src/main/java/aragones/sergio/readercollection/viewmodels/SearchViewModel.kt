/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.repositories.SearchRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
): ViewModel() {

    private var page: Int = 1
    private var query: String = ""
    private val _books = MutableLiveData<MutableList<BookResponse>>()
    private val _searchLoading = MutableLiveData<Boolean>()

    //MARK: - Public properties

    val books: LiveData<MutableList<BookResponse>> = _books
    val searchLoading: LiveData<Boolean> = _searchLoading

    //MARK: - Public methods

    fun searchBooks() {

        _searchLoading.value = true
        searchRepository.searchBooks(query, page, null).subscribeBy(
            onSuccess = {

                page++
                val currentValues = _books.value ?: mutableListOf()
                currentValues.addAll(Constants.mapGoogleBooks(it.items))
                _books.value = currentValues
                _searchLoading.value = false
            },
            onError = {

                _searchLoading.value = false
                //TODO handle Google error
            }
        )
    }

    fun reloadData() {

        page = 1
        _books.value = mutableListOf()
    }

    fun setSearch(query: String) {
        this.query = query
    }
}