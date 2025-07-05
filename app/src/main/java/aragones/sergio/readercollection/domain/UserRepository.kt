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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    @IoScheduler private val ioScheduler: Scheduler,
    @MainScheduler private val mainScheduler: Scheduler,
) : BaseRepository() {

    //region Public properties
    val username: String
        get() = userLocalDataSource.username

    val userData: UserData
        get() = userLocalDataSource.userData

    val userId: String
        get() = userLocalDataSource.userId

    val isProfilePublic: Boolean
        get() = userLocalDataSource.isProfilePublic

    val isAutomaticSyncEnabled: Boolean
        get() = userLocalDataSource.isAutomaticSyncEnabled

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
    //endregion

    //region Public methods
    fun login(username: String, password: String): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .login(username, password)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { uuid ->
                        val userData = UserData(username, password)
                        val authData = AuthData(uuid)
                        userLocalDataSource.storeLoginData(userData, authData)
                        emitter.onComplete()
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun logout() {
        userRemoteDataSource
            .logout()
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    userLocalDataSource.logout()
                },
                onError = {},
            ).addTo(disposables)
    }

    fun register(username: String, password: String): Completable = Completable
        .create { emitter ->
            userRemoteDataSource
                .register(username, password)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        val userData = UserData(username, password)
                        val authData = AuthData("")
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
                                onSuccess = { uuid ->
                                    userLocalDataSource.storeCredentials(AuthData(uuid))
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

    fun setPublicProfile(value: Boolean): Completable = Completable
        .create { emitter ->
            if (value) {
                userRemoteDataSource
                    .registerPublicProfile(username, userId)
                    .timeout(10, TimeUnit.SECONDS)
                    .subscribeOn(ioScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = {
                            emitter.onComplete()
                            userLocalDataSource.storePublicProfile(value)
                        },
                        onError = {
                            emitter.onError(it)
                        },
                    ).addTo(disposables)
            } else {
                userRemoteDataSource
                    .deletePublicProfile(userId)
                    .timeout(10, TimeUnit.SECONDS)
                    .subscribeOn(ioScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onComplete = {
                            userLocalDataSource.storePublicProfile(value)
                            emitter.onComplete()
                        },
                        onError = {
                            emitter.onError(it)
                        },
                    ).addTo(disposables)
            }
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

    fun storeAutomaticSync(value: Boolean) {
        userLocalDataSource.storeAutomaticSync(value)
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

    suspend fun isThereMandatoryUpdate(): Boolean {
        val currentVersion = userLocalDataSource.getCurrentVersion()
        val minVersion = userRemoteDataSource.getMinVersion()
        return currentVersion < minVersion
    }
    //endregion
}