/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
) {

    private val mailEnd = "@readercollection.app"

    //region Public methods
    fun login(username: String, password: String) = Single.create { single ->
        auth.signInWithEmailAndPassword("${username}$mailEnd", password).addOnCompleteListener {
            if (it.isSuccessful) {
                single.onSuccess("-")
            } else {
                single.onError(it.exception ?: IllegalStateException())
            }
        }
    }

    fun logout() = Completable.create { completable ->
        auth.signOut()
        completable.onComplete()
    }

    fun register(username: String, password: String) = Completable.create { completable ->
        auth.createUserWithEmailAndPassword("${username}$mailEnd", password).addOnCompleteListener {
            if (it.isSuccessful) {
                completable.onComplete()
            } else {
                completable.onError(it.exception ?: IllegalStateException())
            }
        }
    }

    fun updatePassword(password: String) = Completable.create { completable ->
        auth.currentUser?.let { currentUser ->
            currentUser.updatePassword(password).addOnCompleteListener {
                if (it.isSuccessful) {
                    completable.onComplete()
                } else {
                    completable.onError(it.exception ?: IllegalStateException())
                }
            }
        } ?: completable.onError(RuntimeException("User is null"))
    }

    fun deleteUser() = Completable.create { completable ->
        auth.currentUser?.delete()
        completable.onComplete()
    }
    //endregion
}