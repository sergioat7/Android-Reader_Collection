/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.landing

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.SharedPreferencesHandler
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.ui.login.LoginActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Private properties
    private val _landingClassToStart = MutableLiveData<Class<*>>()
    //endregion

    //region Public properties
    val language: String
        get() = SharedPreferencesHandler.language
    val newChangesPopupShown: Boolean
        get() = SharedPreferencesHandler.newChangesPopupShown
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

        SharedPreferencesHandler.newChangesPopupShown = true
        _landingClassToStart.value = if (SharedPreferencesHandler.isLoggedIn) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }
    }

    fun checkTheme() {

        when (SharedPreferencesHandler.themeMode) {
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    //endregion

    //region Private methods
    private fun resetDatabase() {

        booksRepository.resetTableObserver().subscribeBy(
            onComplete = {
                _landingClassToStart.value = LoginActivity::class.java
            },
            onError = {
                _landingClassToStart.value = LoginActivity::class.java
            }
        ).addTo(disposables)
    }
    //endregion
}