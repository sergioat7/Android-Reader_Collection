/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.injection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.network.apiclient.GoogleAPIClient
import aragones.sergio.readercollection.repositories.BookDetailRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import javax.inject.Inject

class BookDetailViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //MARK: - Public properties

    @Inject
    lateinit var sharedPreferencesHandler: SharedPreferencesHandler
    @Inject
    lateinit var bookAPIClient: BookAPIClient
    @Inject
    lateinit var googleAPIClient: GoogleAPIClient
    @Inject
    lateinit var bookDetailRepository: BookDetailRepository
    @Inject
    lateinit var bookDetailViewModel: BookDetailViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {

            (application as ReaderCollectionApplication).booksComponent.inject(this)
            return bookDetailViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}