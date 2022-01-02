/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.BookApiClient
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.GoogleBookRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import javax.inject.Inject

class SearchViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //MARK: - Public properties

    @Inject
    lateinit var sharedPreferencesHandler: SharedPreferencesHandler
    @Inject
    lateinit var bookApiClient: BookApiClient
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var googleBookRepository: GoogleBookRepository
    @Inject
    lateinit var searchViewModel: SearchViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return searchViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}