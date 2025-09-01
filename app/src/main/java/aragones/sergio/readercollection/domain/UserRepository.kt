/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import aragones.sergio.readercollection.domain.base.BaseRepository
import aragones.sergio.readercollection.domain.di.IoScheduler
import aragones.sergio.readercollection.domain.di.MainScheduler
import aragones.sergio.readercollection.domain.model.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.rx3.rxCompletable
import kotlinx.coroutines.rx3.rxSingle

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
            rxSingle {
                userRemoteDataSource
                    .login(username, password)
            }.subscribeOn(ioScheduler)
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
        rxCompletable {
            userRemoteDataSource
                .logout()
        }.subscribeOn(ioScheduler)
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
            rxCompletable {
                userRemoteDataSource
                    .register(username, password)
            }.subscribeOn(ioScheduler)
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
            rxCompletable {
                userRemoteDataSource
                    .updatePassword(password)
            }.subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        userLocalDataSource.storePassword(password)
                        rxSingle {
                            userRemoteDataSource
                                .login(userLocalDataSource.username, password)
                        }.subscribeOn(ioScheduler)
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
                rxCompletable {
                    userRemoteDataSource
                        .registerPublicProfile(username, userId)
                }.timeout(10, TimeUnit.SECONDS)
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
                rxCompletable {
                    userRemoteDataSource
                        .deletePublicProfile(userId)
                }.timeout(10, TimeUnit.SECONDS)
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

    fun loadConfig(): Completable = Completable
        .create { emitter ->
            rxSingle {
                userRemoteDataSource
                    .isPublicProfileActive(username)
            }.timeout(10, TimeUnit.SECONDS)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .onErrorReturnItem(false)
                .subscribeBy(
                    onSuccess = { value ->
                        userLocalDataSource.storePublicProfile(value)
                        emitter.onComplete()
                    },
                ).addTo(disposables)
        }

    fun getUserWith(username: String): Single<User> = Single.create { emitter ->
        rxSingle {
            userRemoteDataSource
                .getUser(username, userId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { user ->
                    rxSingle {
                        userRemoteDataSource
                            .getFriends(userId)
                    }.timeout(10, TimeUnit.SECONDS)
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .onErrorReturnItem(emptyList())
                        .subscribeBy(
                            onSuccess = { friends ->
                                friends.firstOrNull { it.id == user.id }?.let { friend ->
                                    emitter.onSuccess(friend.toDomain())
                                } ?: emitter.onSuccess(user.toDomain())
                            },
                        ).addTo(disposables)
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun getFriends(): Single<List<User>> = Single.create { emitter ->
        rxSingle {
            userRemoteDataSource
                .getFriends(userId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .onErrorReturnItem(emptyList())
            .subscribeBy(
                onSuccess = { friends ->
                    emitter.onSuccess(friends.map { it.toDomain() })
                },
            ).addTo(disposables)
    }

    fun getFriend(friendId: String): Single<User> = Single.create { emitter ->
        rxSingle {
            userRemoteDataSource
                .getFriend(userId, friendId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { user ->
                    emitter.onSuccess(user.toDomain())
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun requestFriendship(friend: User): Completable = Completable.create { emitter ->
        val user = UserResponse(
            id = userId,
            username = username,
            status = RequestStatus.PENDING_MINE,
        )
        val friend = friend.toRemoteData()
        rxCompletable {
            userRemoteDataSource
                .requestFriendship(user, friend)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun acceptFriendRequest(friendId: String): Completable = Completable.create { emitter ->
        rxCompletable {
            userRemoteDataSource
                .acceptFriendRequest(userId, friendId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun rejectFriendRequest(friendId: String): Completable = Completable.create { emitter ->
        rxCompletable {
            userRemoteDataSource
                .rejectFriendRequest(userId, friendId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun deleteFriend(friendId: String): Completable = Completable.create { emitter ->
        rxCompletable {
            userRemoteDataSource
                .deleteFriend(userId, friendId)
        }.timeout(10, TimeUnit.SECONDS)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                },
            ).addTo(disposables)
    }

    fun deleteUser(): Completable = Completable
        .create { emitter ->
            rxCompletable {
                userRemoteDataSource
                    .deleteUser(userId)
            }.subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        userLocalDataSource.logout()
                        userLocalDataSource.removeUserData()
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