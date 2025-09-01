/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
    suspend fun login(username: String, password: String): Result<String> = runCatching {
        auth.signInWithEmailAndPassword("${username}$mailEnd", password).await()
        Firebase.auth.currentUser?.uid ?: ""
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun register(username: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword("${username}$mailEnd", password).await()
    }

    suspend fun updatePassword(password: String): Result<Unit> = runCatching {
        auth.currentUser
            ?.updatePassword(
                password,
            )?.await() ?: throw RuntimeException("User is null")
    }

    suspend fun registerPublicProfile(username: String, userId: String): Result<Unit> =
        runCatching {
            firestore
                .collection("public_profiles")
                .document(userId)
                .set(mapOf("uuid" to userId, "email" to "${username}$mailEnd"))
                .await()
        }

    suspend fun isPublicProfileActive(username: String): Result<Boolean> = runCatching {
        val result = firestore
            .collection("public_profiles")
            .whereEqualTo("email", "${username}$mailEnd")
            .get()
            .await()
        result.documents.firstOrNull()?.getString("email") != null
    }

    suspend fun deletePublicProfile(userId: String): Result<Unit> = runCatching {
        firestore
            .collection("public_profiles")
            .document(userId)
            .delete()
            .await()
    }

    suspend fun getUser(username: String, userId: String): Result<UserResponse> = runCatching {
        val result = firestore
            .collection("public_profiles")
            .whereEqualTo("email", "${username}$mailEnd")
            .get()
            .await()

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
        user ?: throw NoSuchElementException("User not found")
    }

    suspend fun getFriends(userId: String): Result<List<UserResponse>> = runCatching {
        firestore
            .collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .await()
            .toObjects(UserResponse::class.java)
    }

    suspend fun getFriend(userId: String, friendId: String): Result<UserResponse> = runCatching {
        val result = firestore
            .collection("users")
            .document(userId)
            .collection("friends")
            .document(friendId)
            .get()
            .await()
        result.toObject(UserResponse::class.java) ?: throw NoSuchElementException("User not found")
    }

    suspend fun requestFriendship(user: UserResponse, friend: UserResponse): Result<Unit> =
        runCatching {
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

            batch.commit().await()
        }

    suspend fun acceptFriendRequest(userId: String, friendId: String): Result<Unit> = runCatching {
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

        batch.commit().await()
    }

    suspend fun rejectFriendRequest(userId: String, friendId: String): Result<Unit> = runCatching {
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

        batch.commit().await()
    }

    suspend fun deleteFriend(userId: String, friendId: String): Result<Unit> = runCatching {
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

        batch.commit().await()
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        val batch = firestore.batch()

        val publicProfileRef = firestore
            .collection("public_profiles")
            .document(userId)
        batch.delete(publicProfileRef)

        val userRef = firestore
            .collection("users")
            .document(userId)
        batch.delete(userRef)

        batch.commit().await()
        auth.currentUser?.delete()?.await()
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