/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 8/1/2026
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.UserResponse

expect class FirebaseProvider {
    fun getUser(): UserResponse?
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun updatePassword(password: String)
    fun signOut()
    suspend fun deleteUser()
    suspend fun registerPublicProfile(username: String, userId: String)
    suspend fun isPublicProfileActive(username: String): Boolean
    suspend fun deletePublicProfile(userId: String)
    suspend fun getUserFromDatabase(username: String, userId: String): UserResponse?
    suspend fun getFriends(userId: String): List<UserResponse>
    suspend fun getFriend(userId: String, friendId: String): UserResponse?
    suspend fun requestFriendship(user: UserResponse, friend: UserResponse)
    suspend fun acceptFriendRequest(userId: String, friendId: String)
    suspend fun rejectFriendRequest(userId: String, friendId: String)
    suspend fun deleteFriendship(userId: String, friendId: String)
    suspend fun deleteFriends(userId: String)
    suspend fun deleteUserFromDatabase(userId: String)
    suspend fun getBooks(userId: String): List<BookResponse>
    suspend fun getBook(userId: String, bookId: String): BookResponse?
    suspend fun syncBooks(
        uuid: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
    )
    suspend fun deleteBooks(userId: String)
    fun fetchRemoteConfigString(key: String, onCompletion: (String) -> Unit)
    suspend fun getRemoteConfigString(key: String): String
}