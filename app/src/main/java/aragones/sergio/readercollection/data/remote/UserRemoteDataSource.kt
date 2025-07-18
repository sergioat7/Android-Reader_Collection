/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import kotlin.NoSuchElementException
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

    fun getUser(username: String, userId: String): Single<UserResponse> = Single.create { emitter ->
        firestore
            .collection("public_profiles")
            .whereEqualTo("email", "${username}$mailEnd")
            .get()
            .addOnSuccessListener { result ->

                val user = result.documents.firstOrNull()?.let {
                    val uuid = it.getString("uuid")
                    val email = it.getString("email")
                    if (uuid != null && email != null && uuid != userId) {
                        UserResponse(
                            id = uuid,
                            username = email.split("@").first(),
                            status = RequestStatus.PENDING_FRIEND,
                        )
                    } else {
                        null
                    }
                }
                if (user != null) {
                    emitter.onSuccess(user)
                } else {
                    emitter.onError(NoSuchElementException("User not found"))
                }
            }.addOnFailureListener { emitter.onError(it) }
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

    fun getFriend(userId: String, friendId: String): Single<UserResponse> =
        Single.create { emitter ->
            firestore
                .collection("users")
                .document(userId)
                .collection("friends")
                .document(friendId)
                .get()
                .addOnSuccessListener { result ->
                    val user = result.toObject(UserResponse::class.java)
                    if (user != null) {
                        emitter.onSuccess(user)
                    } else {
                        emitter.onError(NoSuchElementException("User not found"))
                    }
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }

    fun requestFriendship(user: UserResponse, friend: UserResponse): Completable =
        Completable.create { emitter ->
            val batch = firestore.batch()

            val userRef = firestore
                .collection("users")
                .document(user.id)
                .collection("friends")
                .document(friend.id)
            val userData = mapOf(
                "id" to friend.id,
                "username" to friend.username,
                "status" to friend.status,
            )
            batch.set(userRef, userData)

            val friendRef = firestore
                .collection("users")
                .document(friend.id)
                .collection("friends")
                .document(user.id)
            val friendData = mapOf(
                "id" to user.id,
                "username" to user.username,
                "status" to user.status,
            )
            batch.set(friendRef, friendData)

            batch
                .commit()
                .addOnSuccessListener {
                    emitter.onComplete()
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }

    fun acceptFriendRequest(userId: String, friendId: String): Completable =
        Completable.create { emitter ->

            val userRef = firestore
                .collection("users")
                .document(userId)
                .collection("friends")
                .document(friendId)

            val friendRef = firestore
                .collection("users")
                .document(friendId)
                .collection("friends")
                .document(userId)

            val batch = firestore.batch()

            batch.update(userRef, "status", RequestStatus.APPROVED)
            batch.update(friendRef, "status", RequestStatus.APPROVED)

            batch
                .commit()
                .addOnSuccessListener {
                    emitter.onComplete()
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }

    fun rejectFriendRequest(userId: String, friendId: String): Completable =
        Completable.create { emitter ->

            val userRef = firestore
                .collection("users")
                .document(userId)
                .collection("friends")
                .document(friendId)

            val friendRef = firestore
                .collection("users")
                .document(friendId)
                .collection("friends")
                .document(userId)

            val batch = firestore.batch()

            batch.delete(userRef)
            batch.update(friendRef, "status", RequestStatus.REJECTED)

            batch
                .commit()
                .addOnSuccessListener {
                    emitter.onComplete()
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }

    fun deleteFriend(userId: String, friendId: String): Completable =
        Completable.create { emitter ->
            val userRef = firestore
                .collection("users")
                .document(userId)
                .collection("friends")
                .document(friendId)

            val friendRef = firestore
                .collection("users")
                .document(friendId)
                .collection("friends")
                .document(userId)

            val batch = firestore.batch()

            batch.delete(userRef)
            batch.delete(friendRef)

            batch
                .commit()
                .addOnSuccessListener {
                    emitter.onComplete()
                }.addOnFailureListener {
                    emitter.onError(it)
                }
        }

    fun deleteUser(userId: String) = Completable.create { completable ->

        val batch = firestore.batch()

        val publicProfileRef = firestore
            .collection("public_profiles")
            .document(userId)
        batch.delete(publicProfileRef)

        val userRef = firestore
            .collection("users")
            .document(userId)
        batch.delete(userRef)

        batch
            .commit()
            .addOnSuccessListener {
                auth.currentUser?.delete()?.addOnCompleteListener {
                    completable.onComplete()
                }
            }.addOnFailureListener {
                auth.currentUser?.delete()?.addOnCompleteListener {
                    completable.onComplete()
                }
            }
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