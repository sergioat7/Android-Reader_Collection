/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.bookdetail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.GoogleBookRepository
import aragones.sergio.readercollection.data.source.UserRepository
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
    lateinit var googleBookRepository: GoogleBookRepository
    @Inject
    lateinit var userRepository: UserRepository
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
            bookDetailViewModel.fetchBook()
            return bookDetailViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}