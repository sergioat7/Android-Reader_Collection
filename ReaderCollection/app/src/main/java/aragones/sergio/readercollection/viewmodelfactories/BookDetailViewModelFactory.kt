/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.GoogleBookRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.viewmodels.BookDetailViewModel
import javax.inject.Inject

class BookDetailViewModelFactory(
    private val application: Application,
    private val bookId: String,
    private val isGoogleBook: Boolean
): ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var formatRepository: FormatRepository
    @Inject
    lateinit var googleBookRepository: GoogleBookRepository
    @Inject
    lateinit var stateRepository: StateRepository
    @Inject
    lateinit var bookDetailViewModel: BookDetailViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            bookDetailViewModel.setBookId(bookId)
            bookDetailViewModel.setIsGoogleBook(isGoogleBook)
            bookDetailViewModel.getBook()
            bookDetailViewModel.getFormats()
            bookDetailViewModel.getStates()
            return bookDetailViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}