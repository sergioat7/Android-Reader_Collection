/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2021
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.activities.LoginActivity
import aragones.sergio.readercollection.activities.MainActivity
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlin.math.max

class LandingViewModel @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository
): BaseViewModel() {

    //MARK: - Private properties

    private val _landingClassToStart = MutableLiveData<Class<*>>()

    //MARK: - Public properties

    val language: String
        get() = sharedPreferencesHandler.getLanguage()
    val landingClassToStart = _landingClassToStart

    // MARK: - Lifecycle methods

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }

    //MARK: - Public methods

    fun checkVersion() {

        val currentVersion = sharedPreferencesHandler.getVersion()
        val newVersion = BuildConfig.VERSION_CODE
        if (newVersion > currentVersion) {

            sharedPreferencesHandler.setVersion(newVersion)
            sharedPreferencesHandler.removePassword()
            sharedPreferencesHandler.removeCredentials()
            resetDatabase()
        }

        val initTime = System.currentTimeMillis() / 1000
        val cls: Class<*> = if (sharedPreferencesHandler.isLoggedIn()) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }
        val finalTime = System.currentTimeMillis() / 1000
        val taskTime = finalTime - initTime
        val time = max(0, 1000 - taskTime)

        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        _landingClassToStart.value = cls
    }

    //MARK: - Private methods

    private fun resetDatabase() {

        var result = 0

        booksRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            },
            onError = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            }
        ).addTo(disposables)

        formatRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            },
            onError = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            }
        ).addTo(disposables)

        stateRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            },
            onError = {

                result += 1
                if (result == 3) {

                    //TODO: fin
                }
            }
        ).addTo(disposables)
    }
}