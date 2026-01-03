/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2026
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.tasks.await

actual class FirebaseProvider(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig,
) {

    //region Static properties
    companion object {
        private const val PUBLIC_PROFILES_PATH = "public_profiles"
        private const val USERS_PATH = "users"
        private const val BOOKS_PATH = "books"
        private const val FRIENDS_PATH = "friends"
        private const val EMAIL_KEY = "email"
        private const val UUID_KEY = "uuid"
    }
    //endregion

    //region Public methods
    actual fun getUser(): UserResponse? = auth.currentUser?.let {
        UserResponse(
            id = it.uid,
            username = it.email ?: "",
        )
    }

    actual suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    actual suspend fun signUp(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (_: FirebaseAuthUserCollisionException) {
            throw CustomExceptions.ExistentUser()
        } catch (e: Exception) {
            throw e
        }
    }

    actual suspend fun updatePassword(password: String) {
        val user = auth.currentUser ?: throw RuntimeException("User is null")
        user.updatePassword(password).await()
    }

    actual fun signOut() = auth.signOut()

    actual suspend fun deleteUser() {
        auth.currentUser?.delete()?.await()
    }

    actual suspend fun registerPublicProfile(username: String, userId: String) {
        firestore
            .collection(PUBLIC_PROFILES_PATH)
            .document(userId)
            .set(mapOf(UUID_KEY to userId, EMAIL_KEY to username))
            .await()
    }

    actual suspend fun isPublicProfileActive(username: String): Boolean = firestore
        .collection(PUBLIC_PROFILES_PATH)
        .whereEqualTo(EMAIL_KEY, username)
        .get()
        .await()
        .documents
        .firstOrNull()
        ?.getString(EMAIL_KEY) != null

    actual suspend fun deletePublicProfile(userId: String) {
        firestore
            .collection(PUBLIC_PROFILES_PATH)
            .document(userId)
            .delete()
            .await()
    }

    actual suspend fun getUserFromDatabase(username: String, userId: String): UserResponse? {
        val result = firestore
            .collection(PUBLIC_PROFILES_PATH)
            .whereEqualTo(EMAIL_KEY, username)
            .get()
            .await()
            .documents
            .firstOrNull()

        return result?.let {
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
    }

    actual suspend fun getFriends(userId: String): List<UserResponse> = firestore
        .collection(USERS_PATH)
        .document(userId)
        .collection(FRIENDS_PATH)
        .get()
        .await()
        .toObjects(UserResponse::class.java)

    actual suspend fun getFriend(userId: String, friendId: String): UserResponse? = firestore
        .collection(USERS_PATH)
        .document(userId)
        .collection(FRIENDS_PATH)
        .document(friendId)
        .get()
        .await()
        .toObject(UserResponse::class.java)

    actual suspend fun requestFriendship(user: UserResponse, friend: UserResponse) {
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

    actual suspend fun acceptFriendRequest(userId: String, friendId: String) {
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

    actual suspend fun rejectFriendRequest(userId: String, friendId: String) {
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

    actual suspend fun deleteFriendship(userId: String, friendId: String) {
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

    actual suspend fun deleteFriends(userId: String) {
        val batch = firestore.batch()
        val friends = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(FRIENDS_PATH)
            .get()
            .await()
        friends.documents.forEach {
            batch.delete(it.reference)
        }
        batch.commit().await()
    }

    actual suspend fun deleteUserFromDatabase(userId: String) {
        firestore
            .collection(USERS_PATH)
            .document(userId)
            .delete()
            .await()
    }

    actual suspend fun getBooks(userId: String): List<BookResponse> = firestore
        .collection(USERS_PATH)
        .document(userId)
        .collection(BOOKS_PATH)
        .get()
        .await()
        .mapNotNull { it.toMap().toBook(it.id) }

    actual suspend fun getBook(userId: String, bookId: String): BookResponse? {
        val document = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(BOOKS_PATH)
            .document(bookId)
            .get()
            .await()
        return document.toMap().toBook(document.id)
    }

    @OptIn(ExperimentalTime::class)
    actual suspend fun syncBooks(
        uuid: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
    ) {
        val batch = firestore.batch()
        val booksRef = firestore
            .collection(USERS_PATH)
            .document(uuid)
            .collection(BOOKS_PATH)

        booksToSave.forEach { book ->
            val docRef = booksRef.document(book.id)
            val values = book.toMap().toMutableMap()
            values["publishedDate"] = (values["publishedDate"] as? Instant)?.toTimestamp()
            values["readingDate"] = (values["readingDate"] as? Instant)?.toTimestamp()
            batch.set(docRef, values)
        }

        booksToRemove.forEach { book ->
            val docRef = booksRef.document(book.id)
            batch.delete(docRef)
        }

        batch.commit().await()
    }

    actual suspend fun deleteBooks(userId: String) {
        val batch = firestore.batch()
        val books = firestore
            .collection(USERS_PATH)
            .document(userId)
            .collection(BOOKS_PATH)
            .get()
            .await()
        books.documents.forEach {
            batch.delete(it.reference)
        }
        batch.commit().await()
    }

    actual fun fetchRemoteConfigString(key: String, onCompletion: (String) -> Unit) {
        onCompletion(remoteConfig.getString(key))

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            onCompletion(remoteConfig.getString(key))
        }
    }

    actual suspend fun getRemoteConfigString(key: String): String {
        try {
            remoteConfig.fetch(0)
            remoteConfig.activate().await()
        } catch (_: Exception) {
        }
        return remoteConfig.getString(key)
    }
    //endregion

    //region Public methods
    @OptIn(ExperimentalTime::class)
    private fun DocumentSnapshot.toMap(): Map<String, Any?> = mapOf(
        "title" to getString("title"),
        "subtitle" to getString("subtitle"),
        "authors" to get("authors"),
        "publisher" to getString("publisher"),
        "publishedDate" to (get("publishedDate") as? Timestamp).toInstant(),
        "readingDate" to (get("readingDate") as? Timestamp).toInstant(),
        "description" to getString("description"),
        "summary" to getString("summary"),
        "isbn" to getString("isbn"),
        "pageCount" to get("pageCount"),
        "categories" to get("categories"),
        "averageRating" to getDouble("averageRating"),
        "ratingsCount" to get("ratingsCount"),
        "rating" to getDouble("rating"),
        "thumbnail" to getString("thumbnail"),
        "image" to getString("image"),
        "format" to getString("format"),
        "state" to getString("state"),
        "priority" to get("priority"),
    )

    @OptIn(ExperimentalTime::class)
    private fun Timestamp?.toInstant(): Instant? = this?.let {
        Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
    }

    @OptIn(ExperimentalTime::class)
    private fun Instant?.toTimestamp(): Timestamp? = this?.let {
        Timestamp(it.epochSeconds, it.nanosecondsOfSecond)
    }
    //endregion
}