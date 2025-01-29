/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.domain.base.BaseRepository
import aragones.sergio.readercollection.domain.di.IoScheduler
import aragones.sergio.readercollection.domain.di.MainScheduler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    @IoScheduler private val ioScheduler: Scheduler,
    @MainScheduler private val mainScheduler: Scheduler,
) : BaseRepository() {

    //region Static properties
    companion object {
        private const val GOOGLE_USER_TEST = "googleTest"
        private const val GOOGLE_PASSWORD_TEST = "d9MqzK3k1&07"
    }
    //endregion

    //region Public properties
    val username: String
        get() = userLocalDataSource.username

    val userData: UserData
        get() = userLocalDataSource.userData

    var language: String
        get() = userLocalDataSource.language
        set(value) {
            userLocalDataSource.language = value
        }

    val isLoggedIn: Boolean
        get() = userLocalDataSource.isLoggedIn

    val sortParam: String?
        get() = userLocalDataSource.sortParam

    val isSortDescending: Boolean
        get() = userLocalDataSource.isSortDescending

    val themeMode: Int
        get() = userLocalDataSource.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasBooksTutorialBeenShown

    val hasDragTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasDragTutorialBeenShown

    val hasSearchTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasSearchTutorialBeenShown

    val hasStatisticsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasStatisticsTutorialBeenShown

    val hasSettingsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasSettingsTutorialBeenShown

    val hasNewBookTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasNewBookTutorialBeenShown

    val hasBookDetailsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasBookDetailsTutorialBeenShown

    var newChangesPopupShown: Boolean
        get() = userLocalDataSource.newChangesPopupShown
        set(value) {
            userLocalDataSource.newChangesPopupShown = value
        }
    //endregion

    //region Public methods
    fun login(username: String, password: String): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .login(username, password)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { token ->
                        if (username == GOOGLE_USER_TEST && password == GOOGLE_PASSWORD_TEST) {
                            val userData =
                                UserData(GOOGLE_USER_TEST, GOOGLE_PASSWORD_TEST, true)
                            val authData = AuthData("-")
                            userLocalDataSource.storeLoginData(userData, authData)
                            emitter.onComplete()
                        } else if (userData.username == username && userData.password == password) {
                            val userData = UserData(username, password, true)
                            val authData = AuthData(token)
                            userLocalDataSource.storeLoginData(userData, authData)
                            emitter.onComplete()
                        } else {
                            emitter.onError(IllegalStateException())
                        }
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun logout() {
        userLocalDataSource.logout()
        userRemoteDataSource.logout()
    }

    fun register(username: String, password: String): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .register(username, password)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        val userData = UserData(username, password, false)
                        val authData = AuthData("-")
                        userLocalDataSource.storeLoginData(userData, authData)
                        emitter.onComplete()
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun updatePassword(password: String): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .updatePassword(password)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        userLocalDataSource.storePassword(password)
                        userRemoteDataSource
                            .login(userLocalDataSource.username, password)
                            .subscribeOn(ioScheduler)
                            .observeOn(mainScheduler)
                            .subscribeBy(
                                onSuccess = { token ->

                                    userLocalDataSource.storeCredentials(AuthData(token))
                                    emitter.onComplete()
                                },
                                onError = {
                                    emitter.onError(it)
                                },
                            ).addTo(disposables)
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun deleteUser(): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .deleteUser()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        userLocalDataSource.removeUserData()
                        userLocalDataSource.removeCredentials()
                        emitter.onComplete()
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun storeLoginData(userData: UserData, authData: AuthData) {
        userLocalDataSource.storeLoginData(userData, authData)
    }

    fun storeCredentials(authData: AuthData) {
        userLocalDataSource.storeCredentials(authData)
    }

    fun storePassword(newPassword: String) {
        userLocalDataSource.storePassword(newPassword)
    }

    fun removeUserData() {
        userLocalDataSource.removeUserData()
    }

    fun storeLanguage(language: String) {
        userLocalDataSource.storeLanguage(language)
    }

    fun storeSortParam(sortParam: String?) {
        userLocalDataSource.storeSortParam(sortParam)
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        userLocalDataSource.storeIsSortDescending(isSortDescending)
    }

    fun storeThemeMode(themeMode: Int) {
        userLocalDataSource.storeThemeMode(themeMode)
    }

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown)
    }

    fun setHasDragTutorialBeenShown(hasDragTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasDragTutorialBeenShown(hasDragTutorialBeenShown)
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown)
    }

    fun setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown)
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown)
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown)
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown)
    }
    //endregion
}