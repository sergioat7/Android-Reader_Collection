/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 8/9/2025
 */

@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.Called
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Assert

class UserRemoteDataSourceTest {

    private val auth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private val dataSource = UserRemoteDataSource(auth, firestore, remoteConfig)

    @Test
    fun `GIVEN successful response WHEN login is called THEN return user id`() = runTest {
        val username = "testuser"
        val password = "password123"
        val expectedUid = "testUid"
        val user = mockk<FirebaseUser>()
        givenSignInSuccess(username, password, user)
        every { user.uid } returns expectedUid

        val result = dataSource.login(username, password)

        assertEquals(true, result.isSuccess)
        assertEquals(expectedUid, result.getOrNull())
        verify {
            auth.signInWithEmailAndPassword("$username@readercollection.app", password)
        }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN firebase user null WHEN login is called THEN return exception`() = runTest {
        val username = "testuser"
        val password = "password123"
        givenSignInSuccess(username, password, null)

        val result = dataSource.login(username, password)

        assertEquals(true, result.isFailure)
        assertIs<NoSuchElementException>(result.exceptionOrNull())
        verify(exactly = 1) {
            auth.signInWithEmailAndPassword("$username@readercollection.app", password)
        }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN firebase error WHEN login is called THEN return exception`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        givenSignInFailure(username, password, exception)
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user

        val result = dataSource.login(username, password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            auth.signInWithEmailAndPassword("$username@readercollection.app", password)
        }
        verify(exactly = 1) { user.wasNot(Called) }
        confirmVerified(auth)
    }

    @Test
    fun `WHEN logout is called THEN auth is called`() = runTest {
        every { auth.signOut() } just Runs

        dataSource.logout()

        verify(exactly = 1) { auth.signOut() }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN successful response WHEN register is called THEN return success`() = runTest {
        val username = "testuser"
        val password = "password123"
        every {
            auth.createUserWithEmailAndPassword("$username@readercollection.app", password)
        } returns Tasks.forResult(mockk<AuthResult>())

        val result = dataSource.register(username, password)

        assertEquals(true, result.isSuccess)
        verify(exactly = 1) {
            auth.createUserWithEmailAndPassword("$username@readercollection.app", password)
        }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN failure response WHEN register is called THEN return failure`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        givenCreateUserFailure(username, password, exception)

        val result = dataSource.register(username, password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) {
            auth.createUserWithEmailAndPassword("$username@readercollection.app", password)
        }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN successful response WHEN updatePassword is called THEN return success`() = runTest {
        val password = "password123"
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.updatePassword(password) } returns Tasks.forResult(mockk<Void>())

        val result = dataSource.updatePassword(password)

        assertEquals(true, result.isSuccess)
        verify(exactly = 1) { auth.currentUser }
        verify(exactly = 1) { user.updatePassword(password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN firebase user null WHEN updatePassword is called THEN return failure`() = runTest {
        val password = "password123"
        every { auth.currentUser } returns null

        val result = dataSource.updatePassword(password)

        assertEquals(true, result.isFailure)
        assertIs<RuntimeException>(result.exceptionOrNull())
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN failure response WHEN updatePassword is called THEN return failure`() = runTest {
        val user = mockk<FirebaseUser>()
        val password = "password123"
        val exception = RuntimeException("Login failed")
        givenUpdatePasswordFailure(user, password, exception)

        val result = dataSource.updatePassword(password)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { user.updatePassword(password) }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN successful response WHEN register public profile is called THEN return success`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            val dataSlot = givenSetPublicProfileSuccess(userId)

            val result = dataSource.registerPublicProfile(username, userId)

            assertEquals(true, result.isSuccess)
            assertEquals(userId, dataSlot.captured["uuid"])
            assertEquals("$username@readercollection.app", dataSlot.captured["email"])
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN register public profile is called THEN return failure`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            val exception = RuntimeException("Firestore error")
            givenSetPublicProfileFailure(userId, exception)

            val result = dataSource.registerPublicProfile(username, userId)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and public profile active WHEN get is public profile active is called THEN return true`() =
        runTest {
            val username = "testuser"
            val documentSnapshot = mockk<DocumentSnapshot>()
            givenGetPublicProfileSuccess(username, listOf(documentSnapshot))
            every { documentSnapshot.getString("email") } returns "$username@readercollection.app"

            val result = dataSource.isPublicProfileActive(username)

            assertEquals(true, result.isSuccess)
            assertEquals(true, result.getOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and public profile not active WHEN get is public profile active is called THEN return false`() =
        runTest {
            val username = "testuser"
            givenGetPublicProfileSuccess(username, emptyList())

            val result = dataSource.isPublicProfileActive(username)

            assertEquals(true, result.isSuccess)
            assertEquals(false, result.getOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get is public profile active is called THEN return failure`() =
        runTest {
            val username = "testuser"
            val exception = RuntimeException("Firestore error")
            givenGetPublicProfileFailure(username, exception)

            val result = dataSource.isPublicProfileActive(username)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response WHEN delete public profile is called THEN return success`() =
        runTest {
            val userId = "testUserId"
            givenDeletePublicProfileSuccess(userId)

            val result = dataSource.deletePublicProfile(userId)

            assertEquals(true, result.isSuccess)
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN delete public profile is called THEN return failure`() =
        runTest {
            val userId = "testUserId"
            val exception = RuntimeException("Firestore error")
            givenDeletePublicProfileFailure(userId, exception)

            val result = dataSource.deletePublicProfile(userId)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and existent user WHEN get user is called THEN return user`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            val friendId = "testFriendId"
            val documentSnapshot = mockk<DocumentSnapshot>()
            every { documentSnapshot.getString("email") } returns "$username@readercollection.app"
            every { documentSnapshot.getString("uuid") } returns friendId
            givenGetPublicProfileSuccess(username, listOf(documentSnapshot))

            val result = dataSource.getUser(username, userId)

            assertEquals(true, result.isSuccess)
            assertEquals(
                UserResponse(
                    id = friendId,
                    username = username,
                    status = RequestStatus.PENDING_FRIEND,
                ),
                result.getOrNull(),
            )
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and non existent user WHEN get user is called THEN return failure`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            givenGetPublicProfileSuccess(username, emptyList())

            val result = dataSource.getUser(username, userId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and current user id WHEN get user is called THEN return failure`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            val documentSnapshot = mockk<DocumentSnapshot>()
            every { documentSnapshot.getString("email") } returns "$username@readercollection.app"
            every { documentSnapshot.getString("uuid") } returns userId
            givenGetPublicProfileSuccess(username, listOf(documentSnapshot))

            val result = dataSource.getUser(username, userId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get user is called THEN return failure`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetPublicProfileFailure(username, exception)

        val result = dataSource.getUser(username, userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and user with friends WHEN get friends is called THEN return list`() =
        runTest {
            val userId = "testUserId"
            val friends = listOf(
                UserResponse("testFriendId1", "TestFriend1", RequestStatus.APPROVED),
                UserResponse("testFriendId2", "TestFriend2", RequestStatus.PENDING_MINE),
            )
            givenGetFriendsSuccess(userId, friends)

            val result = dataSource.getFriends(userId)

            assertEquals(true, result.isSuccess)
            assertEquals(friends, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and user without friends WHEN get friends is called THEN return empty list`() =
        runTest {
            val userId = "testUserId"
            val friends = emptyList<UserResponse>()
            givenGetFriendsSuccess(userId, friends)

            val result = dataSource.getFriends(userId)

            assertEquals(true, result.isSuccess)
            assertEquals(friends, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get friends is called THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetFriendsFailure(userId, exception)

        val result = dataSource.getFriends(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and existent friend WHEN get friend is called THEN return friend`() =
        runTest {
            val userId = "testUserId"
            val friend = UserResponse("testFriendId", "TestFriend1")
            givenGetFriendSuccess(userId, friend.id, friend)

            val result = dataSource.getFriend(userId, friend.id)

            assertEquals(true, result.isSuccess)
            assertEquals(friend, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and non existent friend WHEN get friend is called THEN return failure`() =
        runTest {
            val userId = "testUserId"
            val friendId = "testFriendId"
            givenGetFriendSuccess(userId, friendId, null)

            val result = dataSource.getFriend(userId, friendId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get friend is called THEN return failure`() = runTest {
        val userId = "testUserId"
        val friendId = "testFriendId"
        val exception = RuntimeException("Firestore error")
        givenGetFriendFailure(userId, friendId, exception)

        val result = dataSource.getFriend(userId, friendId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN request friendship is called THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenRequestFriendshipSuccess(user, friend)

        val result = dataSource.requestFriendship(user, friend)

        assertEquals(true, result.isSuccess)
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN request friendship is called THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenRequestFriendshipFailure(user, friend, exception)

        val result = dataSource.requestFriendship(user, friend)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN accept friend request is called THEN return success`() =
        runTest {
            val user = UserResponse("testUserId", "TestUser1")
            val friend = UserResponse("testFriendId", "TestFriend1")
            givenAcceptFriendRequestSuccess(user, friend)

            val result = dataSource.acceptFriendRequest(user.id, friend.id)

            assertEquals(true, result.isSuccess)
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 2) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN accept friend request is called THEN return failure`() =
        runTest {
            val user = UserResponse("testUserId", "TestUser1")
            val friend = UserResponse("testFriendId", "TestFriend1")
            val exception = RuntimeException("Firestore error")
            givenAcceptFriendRequestFailure(user, friend, exception)

            val result = dataSource.acceptFriendRequest(user.id, friend.id)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 2) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response WHEN reject friend request is called THEN return success`() =
        runTest {
            val user = UserResponse("testUserId", "TestUser1")
            val friend = UserResponse("testFriendId", "TestFriend1")
            givenRejectFriendRequestSuccess(user, friend)

            val result = dataSource.rejectFriendRequest(user.id, friend.id)

            assertEquals(true, result.isSuccess)
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 2) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN reject friend request is called THEN return failure`() =
        runTest {
            val user = UserResponse("testUserId", "TestUser1")
            val friend = UserResponse("testFriendId", "TestFriend1")
            val exception = RuntimeException("Firestore error")
            givenRejectFriendRequestFailure(user, friend, exception)

            val result = dataSource.rejectFriendRequest(user.id, friend.id)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 2) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response WHEN delete friend is called THEN return success`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenDeleteFriendSuccess(user, friend)

        val result = dataSource.deleteFriend(user.id, friend.id)

        assertEquals(true, result.isSuccess)
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN delete friend is called THEN return failure`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenDeleteFriendFailure(user, friend, exception)

        val result = dataSource.deleteFriend(user.id, friend.id)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN delete user is called THEN return success`() = runTest {
        val userId = "testUserId"
        givenDeleteUserSuccess(userId)

        val result = dataSource.deleteUser(userId)

        assertEquals(true, result.isSuccess)
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("public_profiles") }
        verify(exactly = 1) { firestore.collection("users") }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN firestore failure response WHEN delete user is called THEN return failure`() =
        runTest {
            val userId = "testUserId"
            val authUser = mockk<FirebaseUser>()
            val exception = RuntimeException("Firestore error")
            givenDeleteUserFailure(userId, authUser, exception, null)

            val result = dataSource.deleteUser(userId)

            assertEquals(true, result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("public_profiles") }
            verify(exactly = 1) { firestore.collection("users") }
            verify(exactly = 1) { authUser.wasNot((Called)) }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN auth failure response WHEN delete user is called THEN return failure`() = runTest {
        val userId = "testUserId"
        val authUser = mockk<FirebaseUser>()
        val exception = RuntimeException("Firestore error")
        givenDeleteUserFailure(userId, authUser, null, exception)

        val result = dataSource.deleteUser(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("public_profiles") }
        verify(exactly = 1) { firestore.collection("users") }
        verify(exactly = 1) { auth.currentUser }
        verify(exactly = 1) { authUser.delete() }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN version name is correct WHEN get min version is called THEN returns version code`() =
        runTest {
            val minVersion = "1.2.3"
            givenRemoteConfigFetchSuccess(minVersion)

            val result = dataSource.getMinVersion()

            assertEquals(102030, result)
            verify(exactly = 1) { remoteConfig.fetch(0) }
            verify(exactly = 1) { remoteConfig.activate() }
            verify(exactly = 1) { remoteConfig.getString("min_version") }
            confirmVerified(remoteConfig)
        }

    @Test
    fun `GIVEN version name is malformed WHEN get min version is called THEN returns 0`() =
        runTest {
            val minVersion = "1.2"
            givenRemoteConfigFetchSuccess(minVersion)

            val result = dataSource.getMinVersion()

            Assert.assertEquals(0, result)
            verify(exactly = 1) { remoteConfig.fetch(0) }
            verify(exactly = 1) { remoteConfig.activate() }
            verify(exactly = 1) { remoteConfig.getString("min_version") }
            confirmVerified(remoteConfig)
        }

    @Test
    fun `GIVEN remote config error WHEN get min version is called THEN returns last fetched version code`() =
        runTest {
            val minVersion = "1.2.3"
            givenRemoteConfigFetchFailure(minVersion)

            val result = dataSource.getMinVersion()

            Assert.assertEquals(102030, result)
            verify(exactly = 1) { remoteConfig.fetch(0) }
            verify(exactly = 1) { remoteConfig.activate() }
            verify(exactly = 1) { remoteConfig.getString("min_version") }
            confirmVerified(remoteConfig)
        }

    private fun getPublicProfile(): CollectionReference {
        val collectionReference = mockk<CollectionReference>()
        every { firestore.collection("public_profiles") } returns collectionReference
        return collectionReference
    }

    private fun getUsers(userId: String): DocumentReference {
        val usersCollectionReference = mockk<CollectionReference>()
        val documentReference = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCollectionReference
        every { usersCollectionReference.document(userId) } returns documentReference
        return documentReference
    }

    private fun getFriends(userId: String): CollectionReference {
        val documentReference = getUsers(userId)
        val friendsCollectionReference = mockk<CollectionReference>()
        every { documentReference.collection("friends") } returns friendsCollectionReference
        return friendsCollectionReference
    }

    private fun givenSignInSuccess(username: String, password: String, user: FirebaseUser?) {
        every {
            auth.signInWithEmailAndPassword("$username@readercollection.app", password)
        } returns Tasks.forResult(mockk<AuthResult>())
        every { auth.currentUser } returns user
    }

    private fun givenSignInFailure(username: String, password: String, exception: Exception) {
        val task = mockk<Task<AuthResult>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every {
            auth.signInWithEmailAndPassword("$username@readercollection.app", password)
        } returns task
    }

    private fun givenCreateUserFailure(username: String, password: String, exception: Exception) {
        val task = mockk<Task<AuthResult>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every {
            auth.createUserWithEmailAndPassword("$username@readercollection.app", password)
        } returns task
    }

    private fun givenUpdatePasswordFailure(
        user: FirebaseUser,
        password: String,
        exception: Exception,
    ) {
        val task = mockk<Task<Void>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { auth.currentUser } returns user
        every { user.updatePassword(password) } returns task
    }

    private fun givenSetPublicProfileSuccess(userId: String): CapturingSlot<Map<String, Any>> {
        val collectionReference = getPublicProfile()
        val documentReference = mockk<DocumentReference>()
        val dataSlot = slot<Map<String, Any>>()
        every { collectionReference.document(userId) } returns documentReference
        every { documentReference.set(capture(dataSlot)) } returns Tasks.forResult(mockk<Void>())
        return dataSlot
    }

    private fun givenSetPublicProfileFailure(userId: String, exception: Exception) {
        val collectionReference = getPublicProfile()
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<Void>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { collectionReference.document(userId) } returns documentReference
        every { documentReference.set(any()) } returns task
    }

    private fun givenDeletePublicProfileSuccess(userId: String) {
        val collectionReference = getPublicProfile()
        val documentReference = mockk<DocumentReference>()
        every { collectionReference.document(userId) } returns documentReference
        every { documentReference.delete() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenDeletePublicProfileFailure(userId: String, exception: Exception) {
        val collectionReference = getPublicProfile()
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<Void>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { collectionReference.document(userId) } returns documentReference
        every { documentReference.delete() } returns task
    }

    private fun givenGetPublicProfileSuccess(username: String, documents: List<DocumentSnapshot>) {
        val collectionReference = getPublicProfile()
        val query = mockk<Query>()
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>()
        every { firestore.collection("public_profiles") } returns collectionReference
        every {
            collectionReference.whereEqualTo("email", "$username@readercollection.app")
        } returns query
        every { query.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns documents
    }

    private fun givenGetPublicProfileFailure(username: String, exception: Exception) {
        val collectionReference = getPublicProfile()
        val query = mockk<Query>()
        val task = mockk<Task<QuerySnapshot>>()
        every {
            collectionReference.whereEqualTo("email", "$username@readercollection.app")
        } returns query
        every { query.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenGetFriendsSuccess(userId: String, friends: List<UserResponse>) {
        val collectionReference = getFriends(userId)
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>()
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.toObjects(UserResponse::class.java) } returns friends
    }

    private fun givenGetFriendsFailure(userId: String, exception: Exception) {
        val collectionReference = getFriends(userId)
        val task = mockk<Task<QuerySnapshot>>()
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenGetFriendSuccess(userId: String, friendId: String, friend: UserResponse?) {
        val collectionReference = getFriends(userId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        every { collectionReference.document(friendId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns documentSnapshot
        every { documentSnapshot.toObject(UserResponse::class.java) } returns friend
    }

    private fun givenGetFriendFailure(userId: String, friendId: String, exception: Exception) {
        val collectionReference = getFriends(userId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        every { collectionReference.document(friendId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun getUserAndFriendReferences(
        user: UserResponse,
        friend: UserResponse,
    ): Pair<DocumentReference, DocumentReference> {
        val usersCollectionReference = mockk<CollectionReference>()
        val userDocumentReference = mockk<DocumentReference>()
        val friendDocumentReference = mockk<DocumentReference>()
        val userCollectionReference = mockk<CollectionReference>()
        val friendCollectionReference = mockk<CollectionReference>()
        val userFriendDocumentReference = mockk<DocumentReference>()
        val friendUserDocumentReference = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCollectionReference
        every { usersCollectionReference.document(user.id) } returns userDocumentReference
        every { usersCollectionReference.document(friend.id) } returns friendDocumentReference
        every { userDocumentReference.collection("friends") } returns userCollectionReference
        every { friendDocumentReference.collection("friends") } returns friendCollectionReference
        every { userCollectionReference.document(friend.id) } returns userFriendDocumentReference
        every { friendCollectionReference.document(user.id) } returns friendUserDocumentReference
        return userFriendDocumentReference to friendUserDocumentReference
    }

    private fun givenSetUserAndFriendDataSuccess(
        user: UserResponse,
        friend: UserResponse,
        batch: WriteBatch,
    ) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val userData = mapOf(
            "id" to friend.id,
            "username" to friend.username,
            "status" to friend.status,
        )
        val friendData = mapOf(
            "id" to user.id,
            "username" to user.username,
            "status" to user.status,
        )
        every { batch.set(userDocumentReference, userData) } returns mockk()
        every { batch.set(friendDocumentReference, friendData) } returns mockk()
    }

    private fun givenRequestFriendshipSuccess(user: UserResponse, friend: UserResponse) {
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        givenSetUserAndFriendDataSuccess(user, friend, batch)
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenRequestFriendshipFailure(
        user: UserResponse,
        friend: UserResponse,
        exception: Exception,
    ) {
        val batch = mockk<WriteBatch>()
        val task = mockk<Task<Void>>()
        every { firestore.batch() } returns batch
        givenSetUserAndFriendDataSuccess(user, friend, batch)
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenAcceptFriendRequestSuccess(user: UserResponse, friend: UserResponse) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        every {
            batch.update(
                userDocumentReference,
                "status",
                RequestStatus.APPROVED,
            )
        } returns mockk()
        every {
            batch.update(
                friendDocumentReference,
                "status",
                RequestStatus.APPROVED,
            )
        } returns mockk()
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenAcceptFriendRequestFailure(
        user: UserResponse,
        friend: UserResponse,
        exception: Exception,
    ) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        val task = mockk<Task<Void>>()
        every { firestore.batch() } returns batch
        every {
            batch.update(
                userDocumentReference,
                "status",
                RequestStatus.APPROVED,
            )
        } returns mockk()
        every {
            batch.update(
                friendDocumentReference,
                "status",
                RequestStatus.APPROVED,
            )
        } returns mockk()
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenRejectFriendRequestSuccess(user: UserResponse, friend: UserResponse) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        every { batch.delete(userDocumentReference) } returns mockk()
        every {
            batch.update(
                friendDocumentReference,
                "status",
                RequestStatus.REJECTED,
            )
        } returns mockk()
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenRejectFriendRequestFailure(
        user: UserResponse,
        friend: UserResponse,
        exception: Exception,
    ) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        val task = mockk<Task<Void>>()
        every { firestore.batch() } returns batch
        every { batch.delete(userDocumentReference) } returns mockk()
        every {
            batch.update(
                friendDocumentReference,
                "status",
                RequestStatus.REJECTED,
            )
        } returns mockk()
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenDeleteFriendSuccess(user: UserResponse, friend: UserResponse) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        every { batch.delete(userDocumentReference) } returns mockk()
        every { batch.delete(friendDocumentReference) } returns mockk()
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenDeleteFriendFailure(
        user: UserResponse,
        friend: UserResponse,
        exception: Exception,
    ) {
        val (userDocumentReference, friendDocumentReference) = getUserAndFriendReferences(
            user,
            friend,
        )
        val batch = mockk<WriteBatch>()
        val task = mockk<Task<Void>>()
        every { firestore.batch() } returns batch
        every { batch.delete(userDocumentReference) } returns mockk()
        every { batch.delete(friendDocumentReference) } returns mockk()
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenDeleteUserSuccess(userId: String) {
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        val publicProfileCollection = getPublicProfile()
        val publicProfileReference = mockk<DocumentReference>()
        every { publicProfileCollection.document(userId) } returns publicProfileReference
        every { batch.delete(publicProfileReference) } returns mockk()
        val usersReference = getUsers(userId)
        val booksReference = mockk<CollectionReference>()
        val friendsReference = mockk<CollectionReference>()
        val booksTask = mockk<Task<QuerySnapshot>>()
        val booksQuerySnapshot = mockk<QuerySnapshot>()
        val bookDocumentSnapshot = mockk<DocumentSnapshot>()
        val bookDocumentReference = mockk<DocumentReference>()
        every { usersReference.collection("books") } returns booksReference
        every { booksReference.get() } returns booksTask
        every { booksTask.isComplete } returns true
        every { booksTask.isCanceled } returns false
        every { booksTask.exception } returns null
        every { booksTask.result } returns booksQuerySnapshot
        every { booksQuerySnapshot.documents } returns listOf(bookDocumentSnapshot)
        every { bookDocumentSnapshot.reference } returns bookDocumentReference
        every { batch.delete(bookDocumentSnapshot.reference) } returns mockk()
        val friendsTask = mockk<Task<QuerySnapshot>>()
        val friendsQuerySnapshot = mockk<QuerySnapshot>()
        val friendDocumentSnapshot = mockk<DocumentSnapshot>()
        val friendDocumentReference = mockk<DocumentReference>()
        every { usersReference.collection("friends") } returns booksReference
        every { friendsReference.get() } returns friendsTask
        every { friendsTask.isComplete } returns true
        every { friendsTask.isCanceled } returns false
        every { friendsTask.exception } returns null
        every { friendsTask.result } returns friendsQuerySnapshot
        every { friendsQuerySnapshot.documents } returns listOf(friendDocumentSnapshot)
        every { friendDocumentSnapshot.reference } returns friendDocumentReference
        every { batch.delete(friendDocumentSnapshot.reference) } returns mockk()
        every { batch.delete(usersReference) } returns mockk()
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.delete() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenDeleteUserFailure(
        userId: String,
        user: FirebaseUser,
        exception1: Exception?,
        exception2: Exception?,
    ) {
        val batch = mockk<WriteBatch>()
        every { firestore.batch() } returns batch
        val publicProfileCollection = getPublicProfile()
        val publicProfileReference = mockk<DocumentReference>()
        every { publicProfileCollection.document(userId) } returns publicProfileReference
        every { batch.delete(publicProfileReference) } returns mockk()
        val usersReference = getUsers(userId)
        val booksReference = mockk<CollectionReference>()
        val friendsReference = mockk<CollectionReference>()
        val booksTask = mockk<Task<QuerySnapshot>>()
        val booksQuerySnapshot = mockk<QuerySnapshot>()
        val bookDocumentSnapshot = mockk<DocumentSnapshot>()
        val bookDocumentReference = mockk<DocumentReference>()
        every { usersReference.collection("books") } returns booksReference
        every { booksReference.get() } returns booksTask
        every { booksTask.isComplete } returns true
        every { booksTask.isCanceled } returns false
        every { booksTask.exception } returns null
        every { booksTask.result } returns booksQuerySnapshot
        every { booksQuerySnapshot.documents } returns listOf(bookDocumentSnapshot)
        every { bookDocumentSnapshot.reference } returns bookDocumentReference
        every { batch.delete(bookDocumentSnapshot.reference) } returns mockk()
        val friendsTask = mockk<Task<QuerySnapshot>>()
        val friendsQuerySnapshot = mockk<QuerySnapshot>()
        val friendDocumentSnapshot = mockk<DocumentSnapshot>()
        val friendDocumentReference = mockk<DocumentReference>()
        every { usersReference.collection("friends") } returns booksReference
        every { friendsReference.get() } returns friendsTask
        every { friendsTask.isComplete } returns true
        every { friendsTask.isCanceled } returns false
        every { friendsTask.exception } returns null
        every { friendsTask.result } returns friendsQuerySnapshot
        every { friendsQuerySnapshot.documents } returns listOf(friendDocumentSnapshot)
        every { friendDocumentSnapshot.reference } returns friendDocumentReference
        every { batch.delete(friendDocumentSnapshot.reference) } returns mockk()
        val task1 = mockk<Task<Void>>()
        every { batch.delete(usersReference) } returns mockk()
        every { batch.commit() } returns if (exception1 != null) {
            task1
        } else {
            Tasks.forResult(mockk<Void>())
        }
        every { task1.isComplete } returns true
        every { task1.isCanceled } returns false
        every { task1.exception } returns exception1
        val task2 = mockk<Task<Void>>()
        every { auth.currentUser } returns user
        every { user.delete() } returns task2
        every { task2.isComplete } returns true
        every { task2.isCanceled } returns false
        every { task2.exception } returns exception2
    }

    private fun givenRemoteConfigFetchSuccess(minVersion: String) {
        every { remoteConfig.fetch(0) } returns Tasks.forResult(mockk<Void>())
        every { remoteConfig.activate() } returns Tasks.forResult(true)
        every { remoteConfig.getString("min_version") } returns minVersion
    }

    private fun givenRemoteConfigFetchFailure(minVersion: String) {
        val task = mockk<Task<Boolean>>()
        every { remoteConfig.fetch(0) } returns Tasks.forResult(mockk<Void>())
        every { remoteConfig.activate() } returns task
        every { remoteConfig.getString("min_version") } returns minVersion
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns RuntimeException("Firestore error")
    }
}