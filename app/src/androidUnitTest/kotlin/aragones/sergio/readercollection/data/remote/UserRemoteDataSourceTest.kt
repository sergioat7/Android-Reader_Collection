/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 8/9/2025
 */

@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class UserRemoteDataSourceTest {

    private val firebaseProvider: FirebaseProvider = mockk()
    private val dataSource = UserRemoteDataSource(firebaseProvider)

    @Test
    fun `GIVEN current user not null WHEN check if user exists THEN return true`() {
        every { firebaseProvider.getUser() } returns mockk()

        val result = dataSource.userExists

        assertEquals(true, result)
        verify { firebaseProvider.getUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN current user null WHEN check if user exists THEN return false`() {
        every { firebaseProvider.getUser() } returns null

        val result = dataSource.userExists

        assertEquals(false, result)
        verify { firebaseProvider.getUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN login THEN return user id`() = runTest {
        val username = "testuser"
        val password = "password123"
        val user = UserResponse(id = "testUid", username = "$username@readercollection.app")
        coEvery { firebaseProvider.signIn(any(), any()) } returns mockk()
        coEvery { firebaseProvider.getUser() } returns user

        val result = dataSource.login(username, password)

        assertEquals(true, result.isSuccess)
        assertEquals(user.id, result.getOrNull())
        coVerify {
            firebaseProvider.signIn(user.username, password)
        }
        coVerify { firebaseProvider.getUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN firebase user null WHEN login THEN return exception`() = runTest {
        val username = "testuser"
        val password = "password123"
        coEvery { firebaseProvider.signIn(any(), any()) } returns mockk()
        coEvery { firebaseProvider.getUser() } returns null

        val result = dataSource.login(username, password)

        assertEquals(true, result.isFailure)
        assertIs<NoSuchElementException>(result.exceptionOrNull())
        coVerify {
            firebaseProvider.signIn("$username@readercollection.app", password)
        }
        coVerify { firebaseProvider.getUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN firebase error WHEN login THEN return exception`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        coEvery { firebaseProvider.signIn(any(), any()) } throws exception

        val result = dataSource.login(username, password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify {
            firebaseProvider.signIn("$username@readercollection.app", password)
        }
        verify(exactly = 0) { firebaseProvider.getUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `WHEN logout THEN auth is called`() = runTest {
        every { firebaseProvider.signOut() } just Runs

        dataSource.logout()

        verify(exactly = 1) { firebaseProvider.signOut() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN register THEN return success`() = runTest {
        val username = "testuser"
        val password = "password123"
        coEvery { firebaseProvider.signUp(any(), any()) } returns mockk()

        val result = dataSource.register(username, password)

        assertEquals(true, result.isSuccess)
        coVerify {
            firebaseProvider.signUp("$username@readercollection.app", password)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN already registered failure response WHEN register THEN return failure`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("The email address is already in use by another account.")
        coEvery { firebaseProvider.signUp(any(), any()) } throws exception

        val result = dataSource.register(username, password)

        assertEquals(true, result.isFailure)
        assertIs<CustomExceptions.ExistentUser>(result.exceptionOrNull())
        coVerify {
            firebaseProvider.signUp("$username@readercollection.app", password)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN register THEN return failure`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        coEvery { firebaseProvider.signUp(any(), any()) } throws exception

        val result = dataSource.register(username, password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify {
            firebaseProvider.signUp("$username@readercollection.app", password)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN updatePassword THEN return success`() = runTest {
        val password = "password123"
        coEvery { firebaseProvider.updatePassword(any()) } just Runs

        val result = dataSource.updatePassword(password)

        assertEquals(true, result.isSuccess)
        coVerify { firebaseProvider.updatePassword(password) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN updatePassword THEN return failure`() = runTest {
        val password = "password123"
        val exception = RuntimeException("Login failed")
        coEvery { firebaseProvider.updatePassword(any()) } throws exception

        val result = dataSource.updatePassword(password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { firebaseProvider.updatePassword(password) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN register public profile THEN return success`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        coEvery { firebaseProvider.registerPublicProfile(any(), any()) } just Runs

        val result = dataSource.registerPublicProfile(username, userId)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) {
            firebaseProvider.registerPublicProfile("$username@readercollection.app", userId)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN register public profile THEN return failure`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.registerPublicProfile(any(), any()) } throws exception

        val result = dataSource.registerPublicProfile(username, userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            firebaseProvider.registerPublicProfile("$username@readercollection.app", userId)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN check is public profile active THEN return value`() = runTest {
        val username = "testuser"
        val value = true
        coEvery { firebaseProvider.isPublicProfileActive(any()) } returns value

        val result = dataSource.isPublicProfileActive(username)

        assertEquals(true, result.isSuccess)
        assertEquals(value, result.getOrNull())
        coVerify(
            exactly = 1,
        ) { firebaseProvider.isPublicProfileActive("$username@readercollection.app") }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN check is public profile active THEN return failure`() =
        runTest {
            val username = "testuser"
            val exception = RuntimeException("Firestore error")
            coEvery { firebaseProvider.isPublicProfileActive(any()) } throws exception

            val result = dataSource.isPublicProfileActive(username)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            coVerify(
                exactly = 1,
            ) { firebaseProvider.isPublicProfileActive("$username@readercollection.app") }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN success response WHEN delete public profile THEN return success`() = runTest {
        val userId = "testUserId"
        coEvery { firebaseProvider.deletePublicProfile(any()) } just Runs

        val result = dataSource.deletePublicProfile(userId)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.deletePublicProfile(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN delete public profile THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.deletePublicProfile(any()) } throws exception

        val result = dataSource.deletePublicProfile(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.deletePublicProfile(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response and existent user WHEN get user THEN return user`() = runTest {
        val user = UserResponse(
            id = "testFriendId",
            username = "testuser",
            status = RequestStatus.PENDING_FRIEND,
        )
        val userId = "testUserId"
        coEvery { firebaseProvider.getUserFromDatabase(any(), any()) } returns user

        val result = dataSource.getUser(user.username, userId)

        assertEquals(true, result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify(exactly = 1) {
            firebaseProvider.getUserFromDatabase(
                "${user.username}@readercollection.app",
                userId,
            )
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response and non existent user WHEN get user THEN return failure`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            coEvery { firebaseProvider.getUserFromDatabase(any(), any()) } returns null

            val result = dataSource.getUser(username, userId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            coVerify(exactly = 1) {
                firebaseProvider.getUserFromDatabase("$username@readercollection.app", userId)
            }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN failure response WHEN get user THEN return failure`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.getUserFromDatabase(any(), any()) } throws exception

        val result = dataSource.getUser(username, userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) {
            firebaseProvider.getUserFromDatabase("$username@readercollection.app", userId)
        }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN get friends THEN return list`() = runTest {
        val userId = "testUserId"
        val friends = listOf(
            UserResponse("testFriendId1", "TestFriend1", RequestStatus.APPROVED),
            UserResponse("testFriendId2", "TestFriend2", RequestStatus.PENDING_MINE),
        )
        coEvery { firebaseProvider.getFriends(any()) } returns friends

        val result = dataSource.getFriends(userId)

        assertEquals(true, result.isSuccess)
        assertEquals(friends, result.getOrNull())
        coVerify(exactly = 1) { firebaseProvider.getFriends(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN get friends THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.getFriends(any()) } throws exception

        val result = dataSource.getFriends(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.getFriends(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response and existent friend WHEN get friend THEN return friend`() =
        runTest {
            val userId = "testUserId"
            val friend = UserResponse("testFriendId", "TestFriend1")
            coEvery { firebaseProvider.getFriend(any(), any()) } returns friend

            val result = dataSource.getFriend(userId, friend.id)

            assertEquals(true, result.isSuccess)
            assertEquals(friend, result.getOrNull())
            coVerify(exactly = 1) { firebaseProvider.getFriend(userId, friend.id) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN success response and non existent friend WHEN get friend THEN return failure`() =
        runTest {
            val userId = "testUserId"
            val friendId = "testFriendId"
            coEvery { firebaseProvider.getFriend(any(), any()) } returns null

            val result = dataSource.getFriend(userId, friendId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            coVerify(exactly = 1) { firebaseProvider.getFriend(userId, friendId) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN failure response WHEN get friend THEN return failure`() = runTest {
        val userId = "testUserId"
        val friendId = "testFriendId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.getFriend(any(), any()) } throws exception

        val result = dataSource.getFriend(userId, friendId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.getFriend(userId, friendId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN request friendship THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        coEvery { firebaseProvider.requestFriendship(any(), any()) } just Runs

        val result = dataSource.requestFriendship(user, friend)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.requestFriendship(user, friend) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN request friendship THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.requestFriendship(any(), any()) } throws exception

        val result = dataSource.requestFriendship(user, friend)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.requestFriendship(user, friend) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN accept friend request THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        coEvery { firebaseProvider.acceptFriendRequest(any(), any()) } just Runs

        val result = dataSource.acceptFriendRequest(user.id, friend.id)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.acceptFriendRequest(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN accept friend request THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.acceptFriendRequest(any(), any()) } throws exception

        val result = dataSource.acceptFriendRequest(user.id, friend.id)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.acceptFriendRequest(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN reject friend request THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        coEvery { firebaseProvider.rejectFriendRequest(any(), any()) } just Runs

        val result = dataSource.rejectFriendRequest(user.id, friend.id)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.rejectFriendRequest(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN reject friend request THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.rejectFriendRequest(any(), any()) } throws exception

        val result = dataSource.rejectFriendRequest(user.id, friend.id)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.rejectFriendRequest(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN delete friend THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        coEvery { firebaseProvider.deleteFriendship(any(), any()) } just Runs

        val result = dataSource.deleteFriend(user.id, friend.id)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.deleteFriendship(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN delete friend THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.deleteFriendship(any(), any()) } throws exception

        val result = dataSource.deleteFriend(user.id, friend.id)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.deleteFriendship(user.id, friend.id) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN delete user THEN return success`() = runTest {
        val userId = "testUserId"
        coEvery { firebaseProvider.deleteBooks(any()) } just Runs
        coEvery { firebaseProvider.deleteFriends(any()) } just Runs
        coEvery { firebaseProvider.deleteUserFromDatabase(any()) } just Runs
        coEvery { firebaseProvider.deletePublicProfile(any()) } just Runs
        coEvery { firebaseProvider.deleteUser() } just Runs

        val result = dataSource.deleteUser(userId)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.deleteBooks(userId) }
        coVerify(exactly = 1) { firebaseProvider.deleteFriends(userId) }
        coVerify(exactly = 1) { firebaseProvider.deleteUserFromDatabase(userId) }
        coVerify(exactly = 1) { firebaseProvider.deletePublicProfile(userId) }
        coVerify(exactly = 1) { firebaseProvider.deleteUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN delete user THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.deleteBooks(any()) } just Runs
        coEvery { firebaseProvider.deleteFriends(any()) } just Runs
        coEvery { firebaseProvider.deleteUserFromDatabase(any()) } throws exception

        val result = dataSource.deleteUser(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.deleteBooks(userId) }
        coVerify(exactly = 1) { firebaseProvider.deleteFriends(userId) }
        coVerify(exactly = 1) { firebaseProvider.deleteUserFromDatabase(userId) }
        coVerify(exactly = 0) { firebaseProvider.deletePublicProfile(userId) }
        coVerify(exactly = 0) { firebaseProvider.deleteUser() }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN version name is correct WHEN get calculated min version THEN returns version code`() =
        runTest {
            val key = "min_version"
            val minVersion = "1.2.3"
            coEvery { firebaseProvider.getRemoteConfigString(key) } returns minVersion

            val result = dataSource.getCalculatedMinVersion()

            assertEquals(102030, result)
            coVerify(exactly = 1) { firebaseProvider.getRemoteConfigString(key) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN version name is malformed WHEN get calculated in version THEN returns 0`() =
        runTest {
            val key = "min_version"
            val minVersion = "1.2"
            coEvery { firebaseProvider.getRemoteConfigString(key) } returns minVersion

            val result = dataSource.getCalculatedMinVersion()

            assertEquals(0, result)
            coVerify(exactly = 1) { firebaseProvider.getRemoteConfigString(key) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN remote config error WHEN get calculated min version THEN returns last fetched version code`() =
        runTest {
            val key = "min_version"
            coEvery { firebaseProvider.getRemoteConfigString(key) } returns ""

            val result = dataSource.getCalculatedMinVersion()

            assertEquals(0, result)
            coVerify(exactly = 1) { firebaseProvider.getRemoteConfigString(key) }
            confirmVerified(firebaseProvider)
        }
}