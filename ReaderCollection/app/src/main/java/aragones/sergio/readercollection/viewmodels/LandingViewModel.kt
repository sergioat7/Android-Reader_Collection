/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2021
 */

package aragones.sergio.readercollection.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.activities.LoginActivity
import aragones.sergio.readercollection.activities.MainActivity
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.base.BaseViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class LandingViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): BaseViewModel() {

    //region Private properties
    private val _landingClassToStart = MutableLiveData<Class<*>>()
    //endregion

    //region Public properties
    val language: String
        get() = SharedPreferencesHandler.getLanguage()
    val landingClassToStart = _landingClassToStart
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun checkVersion() {

        val currentVersion = SharedPreferencesHandler.getVersion()
        val newVersion = BuildConfig.VERSION_CODE
        if (newVersion > currentVersion) {

            SharedPreferencesHandler.setVersion(newVersion)
            SharedPreferencesHandler.removePassword()
            SharedPreferencesHandler.removeCredentials()
            resetDatabase()
        } else {

            _landingClassToStart.value = if (SharedPreferencesHandler.isLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
        }
    }

    fun checkTheme() {

        when (SharedPreferencesHandler.getThemeMode()) {
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