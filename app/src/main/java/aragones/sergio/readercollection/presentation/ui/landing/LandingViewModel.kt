/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2021
 */

package aragones.sergio.readercollection.presentation.ui.landing

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.login.LoginActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val _landingClassToStart = MutableLiveData<Class<*>>()
    //endregion

    //region Public properties
    val language: String
        get() = userRepository.language
    val newChangesPopupShown: Boolean
        get() = userRepository.newChangesPopupShown
    val landingClassToStart = _landingClassToStart
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun checkIsLoggedIn() {
        userRepository.newChangesPopupShown = true
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

    //region Private methods
    private fun resetDatabase() {
        booksRepository
            .resetTable()
            .subscribeBy(
                onComplete = {
                    _landingClassToStart.value = LoginActivity::class.java
                },
                onError = {
                    _landingClassToStart.value = LoginActivity::class.java
                },
            ).addTo(disposables)
    }
    //endregion
}