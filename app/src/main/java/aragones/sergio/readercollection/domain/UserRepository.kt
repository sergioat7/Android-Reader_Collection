/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.domain.model.User

interface UserRepository {
    val username: String
    val userData: UserData
    val userId: String
    val isProfilePublic: Boolean
    val isAutomaticSyncEnabled: Boolean
    var language: String
    val isLoggedIn: Boolean
    val sortParam: String?
    val isSortDescending: Boolean
    val themeMode: Int

    suspend fun login(username: String, password: String): Result<Unit>
    fun logout()
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun updatePassword(password: String): Result<Unit>
    suspend fun setPublicProfile(value: Boolean): Result<Unit>
    suspend fun loadConfig()
    suspend fun getUserWith(username: String): Result<User>
    suspend fun getFriends(): List<User>
    suspend fun getFriend(friendId: String): Result<User>
    suspend fun requestFriendship(friend: User): Result<Unit>
    suspend fun acceptFriendRequest(friendId: String): Result<Unit>
    suspend fun rejectFriendRequest(friendId: String): Result<Unit>
    suspend fun deleteFriend(friendId: String): Result<Unit>
    suspend fun deleteUser(): Result<Unit>
    fun storeAutomaticSync(value: Boolean)
    fun storeLanguage(language: String)
    fun storeSortParam(sortParam: String?)
    fun storeIsSortDescending(isSortDescending: Boolean)
    fun storeThemeMode(themeMode: Int)
    suspend fun isThereMandatoryUpdate(): Boolean
}
