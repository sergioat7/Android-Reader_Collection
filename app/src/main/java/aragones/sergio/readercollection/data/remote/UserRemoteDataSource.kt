/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor() {

    //region Public methods
    fun login(username: String, password: String): Single<String> = Single.create {
        it.onSuccess("-")
    }

    fun logout(): Completable = Completable.create {
        it.onComplete()
    }

    fun register(username: String, password: String): Completable = Completable.create {
        it.onComplete()
    }

    fun updatePassword(password: String): Completable = Completable.create {
        it.onComplete()
    }

    fun deleteUser(): Completable = Completable.create {
        it.onComplete()
    }
    //endregion
}