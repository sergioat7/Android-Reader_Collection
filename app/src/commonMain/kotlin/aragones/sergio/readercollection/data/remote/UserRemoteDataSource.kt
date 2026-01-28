/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.data.remote.model.UserResponse

class UserRemoteDataSource(
    private val firebaseProvider: FirebaseProvider,
) {

    //region Static properties
    companion object {
        private const val MAIL_END = "@readercollection.app"
        const val EXISTENT_USER_MESSAGE = "The email address is already in use by another account."
    }
    //endregion

    //region Public properties
    val userExists: Boolean
        get() = firebaseProvider.getUser() != null
    //endregion

    //region Public methods
    suspend fun login(username: String, password: String): Result<String> = runCatching {
        firebaseProvider.signIn("${username}$MAIL_END", password)
        firebaseProvider.getUser()?.id ?: throw NoSuchElementException()
    }

    fun logout() = firebaseProvider.signOut()

    suspend fun register(username: String, password: String): Result<Unit> = runCatching {
        firebaseProvider.signUp("${username}$MAIL_END", password)
    }.recoverCatching {
        if (it.message == EXISTENT_USER_MESSAGE) {
            throw CustomExceptions.ExistentUser()
        } else {
            throw it
        }
    }

    suspend fun updatePassword(password: String): Result<Unit> = runCatching {
        firebaseProvider.updatePassword(password)
    }

    suspend fun registerPublicProfile(username: String, userId: String): Result<Unit> =
        runCatching {
            firebaseProvider.registerPublicProfile("${username}$MAIL_END", userId)
        }

    suspend fun isPublicProfileActive(username: String): Result<Boolean> = runCatching {
        firebaseProvider.isPublicProfileActive("${username}$MAIL_END")
    }

    suspend fun deletePublicProfile(userId: String): Result<Unit> = runCatching {
        firebaseProvider.deletePublicProfile(userId)
    }

    suspend fun getUser(username: String, userId: String): Result<UserResponse> = runCatching {
        val user = firebaseProvider.getUserFromDatabase("${username}$MAIL_END", userId)
        user ?: throw NoSuchElementException("User not found")
    }

    suspend fun getFriends(userId: String): Result<List<UserResponse>> = runCatching {
        firebaseProvider.getFriends(userId)
    }

    suspend fun getFriend(userId: String, friendId: String): Result<UserResponse> = runCatching {
        val result = firebaseProvider.getFriend(userId, friendId)
        result ?: throw NoSuchElementException("User not found")
    }

    suspend fun requestFriendship(user: UserResponse, friend: UserResponse): Result<Unit> =
        runCatching {
            firebaseProvider.requestFriendship(user, friend)
        }

    suspend fun acceptFriendRequest(userId: String, friendId: String): Result<Unit> = runCatching {
        firebaseProvider.acceptFriendRequest(userId, friendId)
    }

    suspend fun rejectFriendRequest(userId: String, friendId: String): Result<Unit> = runCatching {
        firebaseProvider.rejectFriendRequest(userId, friendId)
    }

    suspend fun deleteFriend(userId: String, friendId: String): Result<Unit> = runCatching {
        firebaseProvider.deleteFriendship(userId, friendId)
    }

    suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        firebaseProvider.deleteBooks(userId)
        firebaseProvider.deleteFriends(userId)
        firebaseProvider.deleteUserFromDatabase(userId)
        firebaseProvider.deletePublicProfile(userId)
        firebaseProvider.deleteUser()
    }

    suspend fun getCalculatedMinVersion(): Int {
        val minVersion = firebaseProvider.getRemoteConfigString("min_version").split(".")
        if (minVersion.size != 3) return 0

        return minVersion[0].toInt() * 100000 +
            minVersion[1].toInt() * 1000 +
            minVersion[2].toInt() * 10
    }
    //endregion
}