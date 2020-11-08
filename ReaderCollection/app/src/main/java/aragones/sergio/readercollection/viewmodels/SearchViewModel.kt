/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.repositories.SearchRepository
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
): ViewModel() {

    private var page: Int = 1
    private lateinit var query: String
    private val _googleBooks = MutableLiveData<MutableList<GoogleBookResponse>>()
    private val _searchLoading = MutableLiveData<Boolean>()

    //MARK: - Public properties

    val googleBooks: LiveData<MutableList<GoogleBookResponse>> = _googleBooks
    val searchLoading: LiveData<Boolean> = _searchLoading

    //MARK: - Public methods

    fun searchBooks() {

        _searchLoading.value = true
        searchRepository.searchBooks(query, page, null).subscribeBy(
            onSuccess = {

                page++
                val currentValues = _googleBooks.value ?: mutableListOf()
                currentValues.addAll(it.items)
                _googleBooks.value = currentValues
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
        _googleBooks.value = mutableListOf()
    }

    fun setSearch(query: String) {
        this.query = query
    }
}