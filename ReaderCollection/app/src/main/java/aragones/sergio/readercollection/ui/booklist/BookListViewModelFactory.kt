/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.booklist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.data.source.BooksRepository
import javax.inject.Inject

class BookListViewModelFactory(
    private val application: Application,
    private val state: String,
    private val sortParam: String?,
    private val isSortDescending: Boolean,
    private val query: String,
    private val year: Int,
    private val month: Int,
    private val author: String?,
    private val format: String?
) : ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var bookListViewModel: BookListViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookListViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            bookListViewModel.setParams(
                state,
                sortParam,
                isSortDescending,
                query,
                year,
                month,
                author,
                format
            )
            return bookListViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}