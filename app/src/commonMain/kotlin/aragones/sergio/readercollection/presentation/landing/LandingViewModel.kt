/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2021
 */

package aragones.sergio.readercollection.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.toGenre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LandingViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private val _isLogged = MutableStateFlow<Boolean?>(null)
    //endregion

    //region Public properties
    val language: String
        get() = userRepository.language
    val isLogged: StateFlow<Boolean?> = _isLogged
    //endregion

    //region Public methods
    fun checkIsLoggedIn() {
        _isLogged.value = userRepository.isLoggedIn
    }

    fun checkTheme() {
        userRepository.applyTheme()
    }

    fun fetchRemoteConfigValues() {
        booksRepository.fetchRemoteConfigValues(language)
    }

    fun setLanguage(value: String) {
        userRepository.language = value
    }

    fun mapGenres() {
        viewModelScope.launch {
            val localBooks = booksRepository.getBooks().firstOrNull() ?: return@launch
            val mappedBooks = localBooks.map { book ->
                val categories = book.categories
                    ?.map { it.name.toGenre() }
                    ?.distinct()
                book.copy(categories = categories)
            }
            booksRepository.setBooks(mappedBooks)
        }
    }
    //endregion
}