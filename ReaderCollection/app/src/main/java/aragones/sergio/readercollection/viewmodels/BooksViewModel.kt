/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.repositories.BooksRepository
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): ViewModel() {
    //TODO use ViewModel
}