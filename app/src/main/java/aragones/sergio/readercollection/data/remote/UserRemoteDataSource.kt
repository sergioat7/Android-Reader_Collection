/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig,
) {

    //region Static properties
    companion object {
        private const val MAIL_END = "@readercollection.app"
        private const val PUBLIC_PROFILES_PATH = "public_profiles"
        private const val USERS_PATH = "users"
        private const val BOOKS_PATH = "books"
        private const val FRIENDS_PATH = "friends"
        private const val EMAIL_KEY = "email"
        private const val UUID_KEY = "uuid"
    }
    //endregion

    //region Public methods
    suspend fun login(username: String, password: String): Result<String> = runCatching {
        auth.signInWithEmailAndPassword("${username}$MAIL_END", password).await()
        auth.currentUser?.uid ?: throw NoSuchElementException()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun register(username: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword("${username}$MAIL_END", password).await()
    }

    suspend fun updatePassword(password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw RuntimeException("User is null")
        user.updatePassword(password).await()
    }

    suspend fun registerPublicProfile(username: String, userId: String): Result<Unit> =
        runCatching {
            firestore
                .collection(PUBLIC_PROFILES_PATH)
                .document(userId)
                .set(mapOf(UUID_KEY to userId, EMAIL_KEY to "${username}$MAIL_END"))
                .await()
        }

    suspend fun isPublicProfileActive(username: String): Result<Boolean> = runCatching {
        val result = firestore
            .collection(PUBLIC_PROFILES_PATH)
            .whereEqualTo(EMAIL_KEY, "${username}$MAIL_END")
            .get()
            .await()
        result.documents.firstOrNull()?.getString(EMAIL_KEY) != null
    }

    suspend fun deletePublicProfile(userId: String): Result<Unit> = runCatching {
        firestore
            .collection(PUBLIC_PROFILES_PATH)
            .document(userId)
            .delete()
            .await()
    }

    suspend fun getUser(username: String, userId: String): Result<UserResponse> = runCatching {
        val result = firestore
            .collection(PUBLIC_PROFILES_PATH)
            .whereEqualTo(EMAIL_KEY, "${username}$MAIL_END")
            .get()
            .await()

        val user = result.documents.firstOrNull()?.let {
            val uuid = it.getString(UUID_KEY)
            val email = it.getString(EMAIL_KEY)
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
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .get()
            .await()
            .toObjects(UserResponse::class.java)
    }

    suspend fun getFriend(userId: String, friendId: String): Result<UserResponse> = runCatching {
        val result = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .document(friendId)
            .get()
            .await()
        result.toObject(UserResponse::class.java) ?: throw NoSuchElementException("User not found")
    }

    suspend fun requestFriendship(user: UserResponse, friend: UserResponse): Result<Unit> =
        runCatching {
            val batch = firestore.batch()

            val userRef = firestore
                .collection(USERS_PATH)
                .document(user.id)
                .collection(FRIENDS_PATH)
                .document(friend.id)
            val userData = mapOf(
                "id" to friend.id,
                "username" to friend.username,
                "status" to friend.status,
            )
            batch.set(userRef, userData)

            val friendRef = firestore
                .collection(USERS_PATH)
                .document(friend.id)
                .collection(FRIENDS_PATH)
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
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .document(friendId)

        val friendRef = firestore
            .collection(USERS_PATH)
            .document(friendId)
            .collection(FRIENDS_PATH)
            .document(userId)

        val batch = firestore.batch()

        batch.update(userRef, "status", RequestStatus.APPROVED)
        batch.update(friendRef, "status", RequestStatus.APPROVED)

        batch.commit().await()
    }

    suspend fun rejectFriendRequest(userId: String, friendId: String): Result<Unit> = runCatching {
        val userRef = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .document(friendId)

        val friendRef = firestore
            .collection(USERS_PATH)
            .document(friendId)
            .collection(FRIENDS_PATH)
            .document(userId)

        val batch = firestore.batch()

        batch.delete(userRef)
        batch.update(friendRef, "status", RequestStatus.REJECTED)

        batch.commit().await()
    }

    suspend fun deleteFriend(userId: String, friendId: String): Result<Unit> = runCatching {
        val userRef = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .document(friendId)

        val friendRef = firestore
            .collection(USERS_PATH)
            .document(friendId)
            .collection(FRIENDS_PATH)
            .document(userId)

        val batch = firestore.batch()

        batch.delete(userRef)
        batch.delete(friendRef)

        batch.commit().await()
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        val batch = firestore.batch()

        val publicProfileRef = firestore
            .collection(PUBLIC_PROFILES_PATH)
            .document(userId)
        batch.delete(publicProfileRef)

        val userRef = firestore
            .collection(USERS_PATH)
            .document(userId)
        val books = userRef.collection(BOOKS_PATH).get().await()
        val friends = userRef.collection(FRIENDS_PATH).get().await()
        books.documents.forEach {
            batch.delete(it.reference)
        }
        friends.documents.forEach {
            batch.delete(it.reference)
        }
        batch.delete(userRef)

        batch.commit().await()
        auth.currentUser?.delete()?.await()
    }

    suspend fun getMinVersion(): Int {
        try {
            remoteConfig.fetch(0)
            remoteConfig.activate().await()
        } catch (_: Exception) {
        }

        val minVersion = remoteConfig.getString("min_version").split(".")
        if (minVersion.size != 3) return 0

        return minVersion[0].toInt() * 100000 +
            minVersion[1].toInt() * 1000 +
            minVersion[2].toInt() * 10
    }
    //endregion
}