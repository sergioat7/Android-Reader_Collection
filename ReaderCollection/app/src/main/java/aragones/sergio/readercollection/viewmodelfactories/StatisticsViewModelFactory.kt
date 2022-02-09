/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.viewmodels.StatisticsViewModel
import javax.inject.Inject

class StatisticsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var statisticsViewModel: StatisticsViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return statisticsViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}