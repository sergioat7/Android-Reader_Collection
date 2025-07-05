/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2021
 */

package aragones.sergio.readercollection.presentation.landing

import androidx.appcompat.app.AppCompatDelegate
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.login.LoginActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val _landingClassToStart = MutableStateFlow<Class<*>?>(null)
    //endregion

    //region Public properties
    val language: String
        get() = userRepository.language
    val landingClassToStart: StateFlow<Class<*>?> = _landingClassToStart
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun checkIsLoggedIn() {
        _landingClassToStart.value = if (userRepository.isLoggedIn) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }
    }

    fun checkTheme() {
        when (userRepository.themeMode) {
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            )
        }
    }

    fun fetchRemoteConfigValues() {
        booksRepository.fetchRemoteConfigValues(language)
    }

    fun setLanguage(value: String) {
        userRepository.language = value
    }
    //endregion
}