/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig,
) {

    private val mailEnd = "@readercollection.app"

    //region Public methods
    fun login(username: String, password: String) = Single.create { single ->
        auth.signInWithEmailAndPassword("${username}$mailEnd", password).addOnCompleteListener {
            if (it.isSuccessful) {
                single.onSuccess(Firebase.auth.currentUser?.uid ?: "")
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

    fun registerPublicProfile(username: String, userId: String): Completable =
        Completable.create { emitter ->
            firestore
                .collection("public_profiles")
                .document(userId)
                .set(mapOf("uuid" to userId, "email" to "${username}$mailEnd"))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }

    fun deletePublicProfile(userId: String): Completable = Completable.create { emitter ->
        firestore
            .collection("public_profiles")
            .document(userId)
            .delete()
            .addOnSuccessListener { emitter.onComplete() }
            .addOnFailureListener { emitter.onError(it) }
    }

    fun getFriends(userId: String): Single<List<UserResponse>> = Single.create { emitter ->
        firestore
            .collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { result ->
                val users = result.toObjects(UserResponse::class.java)
                emitter.onSuccess(users)
            }.addOnFailureListener {
                emitter.onError(it)
            }
    }

    fun deleteUser() = Completable.create { completable ->
        auth.currentUser?.delete()
        completable.onComplete()
    }

    suspend fun getMinVersion(): Int {
        remoteConfig.fetch(0)
        remoteConfig.activate().await()

        val minVersion = remoteConfig.getString("min_version").split(".")
        if (minVersion.size != 3) return 0

        return minVersion[0].toInt() * 100000 +
            minVersion[1].toInt() * 1000 +
            minVersion[2].toInt() * 10
    }
    //endregion
}