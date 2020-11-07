/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.injection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import aragones.sergio.readercollection.repositories.SearchRepository
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import javax.inject.Inject

class SearchViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //MARK: - Public properties

    @Inject
    lateinit var googleAPIClient: GoogleAPIClient
    @Inject
    lateinit var searchRepository: SearchRepository
    @Inject
    lateinit var searchViewModel: SearchViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {

            (application as ReaderCollectionApplication).googleApiClientComponent.inject(this)
            return searchViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}