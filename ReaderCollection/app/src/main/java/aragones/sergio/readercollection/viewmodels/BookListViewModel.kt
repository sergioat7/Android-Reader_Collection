/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.repositories.BooksRepository
import javax.inject.Inject

class BookListViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBooks(state: String, sortParam: String?, isSortDescending: Boolean, query: String) {
        //TODO: fetch books
    }
    //endregion
}