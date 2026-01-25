/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2026
 */

@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.data.remote.model.UserResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
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
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

class FirebaseProviderTest {

    private val auth: FirebaseAuth = mockk()
    private val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk()
    private val firebaseProvider = FirebaseProviderAndroid(auth, firestore, remoteConfig)

    @Test
    fun `GIVEN current user not null WHEN get user THEN return user`() {
        val user = UserResponse("testUid", "testuser")
        val firebaseUser: FirebaseUser = mockk()
        every { firebaseUser.uid } returns user.id
        every { firebaseUser.email } returns user.username
        every { auth.currentUser } returns firebaseUser

        val result = firebaseProvider.getUser()

        assertEquals(user, result)
        verify { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN current user null WHEN get user THEN return null`() {
        every { auth.currentUser } returns null

        val result = firebaseProvider.getUser()

        assertEquals(null, result)
        verify { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN success response WHEN sign in THEN return result`() = runTest {
        val username = "testuser"
        val password = "password123"
        every {
            auth.signInWithEmailAndPassword(any(), any())
        } returns Tasks.forResult(mockk<AuthResult>())

        firebaseProvider.signIn(username, password)

        verify { auth.signInWithEmailAndPassword(username, password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN firebase error WHEN sign in THEN throw exception`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        val task = mockk<Task<AuthResult>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { auth.signInWithEmailAndPassword(any(), any()) } returns task

        try {
            firebaseProvider.signIn(username, password)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { auth.signInWithEmailAndPassword(username, password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN success response WHEN sign up THEN return result`() = runTest {
        val username = "testuser"
        val password = "password123"
        every {
            auth.createUserWithEmailAndPassword(any(), any())
        } returns Tasks.forResult(mockk<AuthResult>())

        firebaseProvider.signUp(username, password)

        verify(exactly = 1) { auth.createUserWithEmailAndPassword(username, password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN FirebaseAuthUserCollisionException response WHEN sign up THEN return ExistentUser exception`() =
        runTest {
            val username = "testuser"
            val password = "wrongpassword"
            val exception: FirebaseAuthUserCollisionException = mockk()
            val task = mockk<Task<AuthResult>>()
            coEvery { task.isComplete } returns true
            coEvery { task.exception } returns exception
            every { auth.createUserWithEmailAndPassword(any(), any()) } returns task

            try {
                firebaseProvider.signUp(username, password)
            } catch (e: Exception) {
                assertIs<CustomExceptions.ExistentUser>(e)
            }
            verify(exactly = 1) { auth.createUserWithEmailAndPassword(username, password) }
            confirmVerified(auth)
        }

    @Test
    fun `GIVEN failure response WHEN sign up THEN throw exception`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val exception = RuntimeException("Login failed")
        val task = mockk<Task<AuthResult>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { auth.createUserWithEmailAndPassword(any(), any()) } returns task

        try {
            firebaseProvider.signUp(username, password)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { auth.createUserWithEmailAndPassword(username, password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN success response WHEN update password THEN nothing`() = runTest {
        val password = "password123"
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.updatePassword(any()) } returns Tasks.forResult(mockk<Void>())

        firebaseProvider.updatePassword(password)

        verify(exactly = 1) { auth.currentUser }
        verify(exactly = 1) { user.updatePassword(password) }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN firebase user null WHEN update password THEN throw exception`() = runTest {
        val password = "password123"
        every { auth.currentUser } returns null

        try {
            firebaseProvider.updatePassword(password)
        } catch (e: Exception) {
            assertIs<RuntimeException>(e)
        }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN failure response WHEN update password THEN throw exception`() = runTest {
        val user = mockk<FirebaseUser>()
        val password = "password123"
        val exception = RuntimeException("Login failed")
        val task = mockk<Task<Void>>()
        coEvery { task.isComplete } returns true
        coEvery { task.exception } returns exception
        every { auth.currentUser } returns user
        every { user.updatePassword(any()) } returns task

        try {
            firebaseProvider.updatePassword(password)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { user.updatePassword(password) }
        verify(exactly = 1) { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `WHEN sign out THEN auth is called`() = runTest {
        every { auth.signOut() } just Runs

        firebaseProvider.signOut()

        verify(exactly = 1) { auth.signOut() }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN user is not null WHEN delete user THEN delete is called`() = runTest {
        val user = mockk<FirebaseUser>()
        every { auth.currentUser } returns user
        every { user.delete() } returns Tasks.forResult(mockk<Void>())

        firebaseProvider.deleteUser()

        verify { auth.currentUser }
        verify { user.delete() }
        confirmVerified(auth, user)
    }

    @Test
    fun `GIVEN user is null WHEN delete user THEN delete is not called`() = runTest {
        every { auth.currentUser } returns null

        firebaseProvider.deleteUser()

        verify { auth.currentUser }
        confirmVerified(auth)
    }

    @Test
    fun `GIVEN success response WHEN register public profile THEN nothing`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val dataSlot = givenSetPublicProfileSuccess(userId)

        firebaseProvider.registerPublicProfile(username, userId)

        assertEquals(userId, dataSlot.captured["uuid"])
        assertEquals(username, dataSlot.captured["email"])
        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN register public profile THEN throw exception`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenSetPublicProfileFailure(userId, exception)

        try {
            firebaseProvider.registerPublicProfile(username, userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and public profile active WHEN check is public profile active THEN return true`() =
        runTest {
            val username = "testuser"
            val documentSnapshot = mockk<DocumentSnapshot>()
            givenGetPublicProfileSuccess(username, listOf(documentSnapshot))
            every { documentSnapshot.getString("email") } returns username

            val result = firebaseProvider.isPublicProfileActive(username)

            assertEquals(true, result)
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and public profile not active WHEN check is public profile active THEN return false`() =
        runTest {
            val username = "testuser"
            givenGetPublicProfileSuccess(username, emptyList())

            val result = firebaseProvider.isPublicProfileActive(username)

            assertEquals(false, result)
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN check is public profile active THEN throw exception`() =
        runTest {
            val username = "testuser"
            val exception = RuntimeException("Firestore error")
            givenGetPublicProfileFailure(username, exception)

            try {
                firebaseProvider.isPublicProfileActive(username)
            } catch (e: Exception) {
                assertEquals(exception, e)
            }
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response WHEN delete public profile THEN return success`() = runTest {
        val userId = "testUserId"
        givenDeletePublicProfileSuccess(userId)

        firebaseProvider.deletePublicProfile(userId)

        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN delete public profile THEN throw exception`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenDeletePublicProfileFailure(userId, exception)

        try {
            firebaseProvider.deletePublicProfile(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and existent user WHEN get user from database THEN return user`() =
        runTest {
            val user = UserResponse(
                id = "testFriendId",
                username = "testuser",
                status = RequestStatus.PENDING_FRIEND,
            )
            val userId = "testUserId"
            val documentSnapshot = mockk<DocumentSnapshot>()
            every { documentSnapshot.getString("email") } returns user.username
            every { documentSnapshot.getString("uuid") } returns user.id
            givenGetPublicProfileSuccess(user.username, listOf(documentSnapshot))

            val result = firebaseProvider.getUserFromDatabase(user.username, userId)

            assertEquals(user, result)
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and non existent user WHEN get user from database THEN return null`() =
        runTest {
            val username = "testuser"
            val userId = "testUserId"
            givenGetPublicProfileSuccess(username, emptyList())

            val result = firebaseProvider.getUserFromDatabase(username, userId)

            assertEquals(null, result)
            verify(exactly = 1) { firestore.collection("public_profiles") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get user from database THEN throw exception`() = runTest {
        val username = "testuser"
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetPublicProfileFailure(username, exception)

        try {
            firebaseProvider.getUserFromDatabase(username, userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("public_profiles") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and user with friends WHEN get friends THEN return list`() =
        runTest {
            val userId = "testUserId"
            val friends = listOf(
                UserResponse("testFriendId1", "TestFriend1", RequestStatus.APPROVED),
                UserResponse("testFriendId2", "TestFriend2", RequestStatus.PENDING_MINE),
            )
            givenGetFriendsSuccess(userId, friends)

            val result = firebaseProvider.getFriends(userId)

            assertEquals(friends, result)
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and user without friends WHEN get friends THEN return empty list`() =
        runTest {
            val userId = "testUserId"
            val friends = emptyList<UserResponse>()
            givenGetFriendsSuccess(userId, friends)

            val result = firebaseProvider.getFriends(userId)

            assertEquals(friends, result)
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get friends THEN throw exception`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetFriendsFailure(userId, exception)

        try {
            firebaseProvider.getFriends(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and existent friend WHEN get friend THEN return friend`() =
        runTest {
            val userId = "testUserId"
            val friend = UserResponse("testFriendId", "TestFriend1")
            givenGetFriendSuccess(userId, friend.id, friend)

            val result = firebaseProvider.getFriend(userId, friend.id)

            assertEquals(friend, result)
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and non existent friend WHEN get friend THEN return null`() =
        runTest {
            val userId = "testUserId"
            val friendId = "testFriendId"
            givenGetFriendSuccess(userId, friendId, null)

            val result = firebaseProvider.getFriend(userId, friendId)

            assertEquals(result, result)
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get friend THEN throw exception`() = runTest {
        val userId = "testUserId"
        val friendId = "testFriendId"
        val exception = RuntimeException("Firestore error")
        givenGetFriendFailure(userId, friendId, exception)

        try {
            firebaseProvider.getFriend(userId, friendId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN request friendship THEN nothing`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenRequestFriendshipSuccess(user, friend)

        firebaseProvider.requestFriendship(user, friend)

        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN request friendship THEN throw exception`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenRequestFriendshipFailure(user, friend, exception)

        try {
            firebaseProvider.requestFriendship(user, friend)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN accept friend request THEN nothing`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenAcceptFriendRequestSuccess(user, friend)

        firebaseProvider.acceptFriendRequest(user.id, friend.id)

        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN accept friend request THEN throw exception`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenAcceptFriendRequestFailure(user, friend, exception)

        try {
            firebaseProvider.acceptFriendRequest(user.id, friend.id)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN reject friend request THEN nothing`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenRejectFriendRequestSuccess(user, friend)

        firebaseProvider.rejectFriendRequest(user.id, friend.id)

        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN reject friend request THEN throw exception`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenRejectFriendRequestFailure(user, friend, exception)

        try {
            firebaseProvider.rejectFriendRequest(user.id, friend.id)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN delete friend THEN nothing`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        givenDeleteFriendSuccess(user, friend)

        firebaseProvider.deleteFriendship(user.id, friend.id)

        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN delete friend THEN throw exception`() = runTest {
        val user = UserResponse("testUserId", "TestUser1")
        val friend = UserResponse("testFriendId", "TestFriend1")
        val exception = RuntimeException("Firestore error")
        givenDeleteFriendFailure(user, friend, exception)

        try {
            firebaseProvider.deleteFriendship(user.id, friend.id)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 2) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and friends to delete WHEN delete friends THEN delete is called`() =
        runTest {
            val userId = "testUserId"
            val batch = mockk<WriteBatch>()
            val friend: DocumentSnapshot = mockk()
            givenDeleteFriendsSuccess(userId, batch, listOf(friend, friend))

            firebaseProvider.deleteFriends(userId)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            verify(exactly = 2) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch, firestore)
        }

    @Test
    fun `GIVEN success response without friends to delete WHEN delete friends THEN delete is not called`() =
        runTest {
            val userId = "testUserId"
            val batch = mockk<WriteBatch>()
            givenDeleteFriendsSuccess(userId, batch, listOf())

            firebaseProvider.deleteFriends(userId)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            verify(exactly = 0) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch, firestore)
        }

    @Test
    fun `GIVEN failure response WHEN delete friends THEN throw exception`() = runTest {
        val userId = "testUserId"
        val batch = mockk<WriteBatch>()
        val friend: DocumentSnapshot = mockk()
        val exception = RuntimeException("Firestore error")
        givenDeleteFriendsFailure(userId, batch, listOf(friend, friend), exception)

        try {
            firebaseProvider.deleteFriends(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("users") }
        verify(exactly = 2) { batch.delete(any()) }
        verify(exactly = 1) { batch.commit() }
        confirmVerified(batch, firestore)
    }

    @Test
    fun `GIVEN success response WHEN delete user from database THEN nothing`() = runTest {
        val userId = "testUserId"
        val collectionReference = getUser(userId)
        every { collectionReference.delete() } returns Tasks.forResult(mockk<Void>())

        firebaseProvider.deleteUserFromDatabase(userId)

        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN delete user from database THEN throw exception`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        val task = mockk<Task<Void>>()
        val collectionReference = getUser(userId)
        every { collectionReference.delete() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception

        try {
            firebaseProvider.deleteUserFromDatabase(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN get books THEN return list`() = runTest {
        val userId = "testUserId"
        givenGetBooksSuccess(userId)

        val result = firebaseProvider.getBooks(userId)

        assertEquals(true, result.isEmpty())
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN get books THEN throw exception`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetBooksFailure(userId, exception)

        try {
            firebaseProvider.getBooks(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response WHEN get book THEN return book`() = runTest {
        val userId = "testUserId"
        val bookId = "bookId"
        val book = BookResponse(id = bookId)
        givenGetBookSuccess(userId, bookId, book)

        val result = firebaseProvider.getBook(userId, bookId)

        assertEquals(true, result != null)
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN failure response WHEN get book THEN throw exception`() = runTest {
        val userId = "testUserId"
        val bookId = "bookId"
        val exception = RuntimeException("Firestore error")
        givenGetBookFailure(userId, bookId, exception)

        try {
            firebaseProvider.getBook(userId, bookId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and books to save and books to remove WHEN sync books THEN set is called and delete is called`() =
        runTest {
            val userId = "testUserId"
            val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
            val booksToRemove = listOf(BookResponse(id = "3"))
            val batch = mockk<WriteBatch>()
            givenSyncBooksSuccess(userId, booksToSave, booksToRemove, batch)

            firebaseProvider.syncBooks(userId, booksToSave, booksToRemove)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
            verify(exactly = 2) { batch.set(any(), any()) }
            verify(exactly = 1) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch)
        }

    @Test
    fun `GIVEN success response without books to save nor books to remove WHEN sync books THEN set and delete are not called`() =
        runTest {
            val userId = "testUserId"
            val booksToSave = emptyList<BookResponse>()
            val booksToRemove = emptyList<BookResponse>()
            val batch = mockk<WriteBatch>()
            givenSyncBooksSuccess(userId, booksToSave, booksToRemove, batch)

            firebaseProvider.syncBooks(userId, booksToSave, booksToRemove)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
            verify(exactly = 0) { batch.set(any(), any()) }
            verify(exactly = 0) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch)
        }

    @Test
    fun `GIVEN failure response WHEN sync books THEN throw exception`() = runTest {
        val userId = "testUserId"
        val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
        val booksToRemove = listOf(BookResponse(id = "3"))
        val batch = mockk<WriteBatch>()
        val exception = RuntimeException("Firestore error")
        givenSyncBooksFailure(userId, booksToSave, booksToRemove, batch, exception)

        try {
            firebaseProvider.syncBooks(userId, booksToSave, booksToRemove)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
        verify(exactly = 2) { batch.set(any(), any()) }
        verify(exactly = 1) { batch.delete(any()) }
        verify(exactly = 1) { batch.commit() }
        confirmVerified(batch)
    }

    @Test
    fun `GIVEN success response and books to delete WHEN delete books THEN delete is called`() =
        runTest {
            val userId = "testUserId"
            val batch = mockk<WriteBatch>()
            val book: DocumentSnapshot = mockk()
            givenDeleteBooksSuccess(userId, batch, listOf(book, book))

            firebaseProvider.deleteBooks(userId)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            verify(exactly = 2) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch, firestore)
        }

    @Test
    fun `GIVEN success response without books to delete WHEN delete books THEN delete is not called`() =
        runTest {
            val userId = "testUserId"
            val batch = mockk<WriteBatch>()
            givenDeleteBooksSuccess(userId, batch, listOf())

            firebaseProvider.deleteBooks(userId)

            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            verify(exactly = 0) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch, firestore)
        }

    @Test
    fun `GIVEN failure response WHEN delete books THEN throw exception`() = runTest {
        val userId = "testUserId"
        val batch = mockk<WriteBatch>()
        val book: DocumentSnapshot = mockk()
        val exception = RuntimeException("Firestore error")
        givenDeleteBooksFailure(userId, batch, listOf(book, book), exception)

        try {
            firebaseProvider.deleteBooks(userId)
        } catch (e: Exception) {
            assertEquals(exception, e)
        }
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("users") }
        verify(exactly = 2) { batch.delete(any()) }
        verify(exactly = 1) { batch.commit() }
        confirmVerified(batch, firestore)
    }

    @Test
    fun `GIVEN success response and values for language WHEN fetch remote config values is called THEN formats and states are updated with new values`() {
        val key = "key"
        val value = "data"
        givenRemoteConfigFetchSuccess(key, value)
        var result: String? = null
        val callback: (String) -> Unit = {
            result = it
        }

        firebaseProvider.fetchRemoteConfigString(key, callback)

        assertEquals(value, result)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString(key) }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN failure response WHEN fetch remote config values is called THEN formats and states are updated with default values`() {
        val key = "key"
        val value = "data"
        givenRemoteConfigFetchFailure(key, value)
        var result: String? = null
        val callback: (String) -> Unit = {
            result = it
        }

        firebaseProvider.fetchRemoteConfigString(key, callback)

        assertEquals(value, result)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString(key) }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN success response WHEN get remote config string THEN returns value`() = runTest {
        val key = "min_version"
        val value = "1.2.3"
        givenRemoteConfigGetSuccess(key, value)

        val result = firebaseProvider.getRemoteConfigString(key)

        assertEquals(value, result)
        verify(exactly = 1) { remoteConfig.fetch(0) }
        verify(exactly = 1) { remoteConfig.activate() }
        verify(exactly = 1) { remoteConfig.getString(key) }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN failure response WHEN get remote config string THEN returns cached value`() =
        runTest {
            val key = "min_version"
            val value = "1.2.3"
            givenRemoteConfigGetFailure(key, value)

            val result = firebaseProvider.getRemoteConfigString(key)

            assertEquals(value, result)
            verify(exactly = 1) { remoteConfig.fetch(0) }
            verify(exactly = 1) { remoteConfig.activate() }
            verify(exactly = 1) { remoteConfig.getString(key) }
            confirmVerified(remoteConfig)
        }

    private fun getPublicProfile(): CollectionReference {
        val collectionReference = mockk<CollectionReference>()
        every { firestore.collection("public_profiles") } returns collectionReference
        return collectionReference
    }

    private fun getUser(userId: String): DocumentReference {
        val usersCollectionReference = mockk<CollectionReference>()
        val documentReference = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCollectionReference
        every { usersCollectionReference.document(userId) } returns documentReference
        return documentReference
    }

    private fun getFriends(userId: String): CollectionReference {
        val documentReference = getUser(userId)
        val friendsCollectionReference = mockk<CollectionReference>()
        every { documentReference.collection("friends") } returns friendsCollectionReference
        return friendsCollectionReference
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
            collectionReference.whereEqualTo("email", username)
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
            collectionReference.whereEqualTo("email", username)
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

    private fun givenDeleteFriendsSuccess(
        userId: String,
        batch: WriteBatch,
        friends: List<DocumentSnapshot>,
    ) {
        every { firestore.batch() } returns batch
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
        val collectionReference = getFriends(userId)
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns friends
        friends.forEach { friend ->
            val friendDocumentReference = mockk<DocumentReference>()
            every { friend.reference } returns friendDocumentReference
            every { batch.delete(friendDocumentReference) } returns mockk()
        }
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenDeleteFriendsFailure(
        userId: String,
        batch: WriteBatch,
        friends: List<DocumentSnapshot>,
        exception: Exception,
    ) {
        every { firestore.batch() } returns batch
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
        val collectionReference = getFriends(userId)
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns friends
        friends.forEach { friend ->
            val friendDocumentReference = mockk<DocumentReference>()
            every { friend.reference } returns friendDocumentReference
            every { batch.delete(friendDocumentReference) } returns mockk()
        }
        val task2 = mockk<Task<Void>>()
        every { batch.commit() } returns task2
        every { task2.isComplete } returns true
        every { task2.isCanceled } returns false
        every { task2.exception } returns exception
    }

    private fun getBooks(userId: String): CollectionReference {
        val usersCollectionReference = mockk<CollectionReference>()
        val userDocumentReference = mockk<DocumentReference>()
        val booksCollectionReference = mockk<CollectionReference>()
        every { firestore.collection("users") } returns usersCollectionReference
        every { usersCollectionReference.document(userId) } returns userDocumentReference
        every { userDocumentReference.collection("books") } returns booksCollectionReference
        return booksCollectionReference
    }

    private fun givenGetBooksSuccess(userId: String) {
        val collectionReference = getBooks(userId)
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
    }

    private fun givenGetBooksFailure(userId: String, exception: Exception) {
        val collectionReference = getBooks(userId)
        val task = mockk<Task<QuerySnapshot>>()
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    @OptIn(ExperimentalTime::class)
    private fun givenGetBookSuccess(userId: String, bookId: String, book: BookResponse?) {
        val collectionReference = getBooks(userId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        every { collectionReference.document(bookId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns documentSnapshot
        if (book != null) {
            every { documentSnapshot.id } returns bookId
            every { documentSnapshot.getString("title") } returns book.title
            every { documentSnapshot.getString("subtitle") } returns book.subtitle
            every { documentSnapshot.get("authors") } returns book.authors
            every { documentSnapshot.getString("publisher") } returns book.publisher
            every { documentSnapshot.get("publishedDate") } returns book.publishedDate?.let {
                val instant = it.atStartOfDayIn(TimeZone.currentSystemDefault())
                Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)
            }
            every { documentSnapshot.get("readingDate") } returns book.readingDate?.let {
                val instant = it.atStartOfDayIn(TimeZone.currentSystemDefault())
                Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)
            }
            every { documentSnapshot.getString("description") } returns book.description
            every { documentSnapshot.getString("summary") } returns book.summary
            every { documentSnapshot.getString("isbn") } returns book.isbn
            every { documentSnapshot.get("pageCount") } returns book.pageCount
            every { documentSnapshot.get("categories") } returns book.categories
            every { documentSnapshot.getDouble("averageRating") } returns book.averageRating
            every { documentSnapshot.get("ratingsCount") } returns book.ratingsCount
            every { documentSnapshot.getDouble("rating") } returns book.rating
            every { documentSnapshot.getString("thumbnail") } returns book.thumbnail
            every { documentSnapshot.getString("image") } returns book.image
            every { documentSnapshot.getString("format") } returns book.format
            every { documentSnapshot.getString("state") } returns book.state
            every { documentSnapshot.get("priority") } returns book.priority
        }
    }

    private fun givenGetBookFailure(friendId: String, bookId: String, exception: Exception) {
        val collectionReference = getBooks(friendId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        every { collectionReference.document(bookId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenSetBooksSuccess(
        books: List<BookResponse>,
        booksRef: CollectionReference,
        batch: WriteBatch,
    ) {
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { booksRef.document(book.id) } returns bookDocumentReference
            every { batch.set(bookDocumentReference, book.toMap()) } returns mockk()
        }
    }

    private fun givenDeleteBooksSuccess(
        books: List<BookResponse>,
        booksRef: CollectionReference,
        batch: WriteBatch,
    ) {
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { booksRef.document(book.id) } returns bookDocumentReference
            every { batch.delete(bookDocumentReference) } returns mockk()
        }
    }

    private fun givenSyncBooksSuccess(
        userId: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
        batch: WriteBatch,
    ) {
        every { firestore.batch() } returns batch
        val collectionReference = getBooks(userId)
        givenSetBooksSuccess(booksToSave, collectionReference, batch)
        givenDeleteBooksSuccess(booksToRemove, collectionReference, batch)
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenSyncBooksFailure(
        userId: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
        batch: WriteBatch,
        exception: Exception,
    ) {
        every { firestore.batch() } returns batch
        val collectionReference = getBooks(userId)
        givenSetBooksSuccess(booksToSave, collectionReference, batch)
        givenDeleteBooksSuccess(booksToRemove, collectionReference, batch)
        val task = mockk<Task<Void>>()
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenDeleteBooksSuccess(
        userId: String,
        batch: WriteBatch,
        books: List<DocumentSnapshot>,
    ) {
        every { firestore.batch() } returns batch
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
        val collectionReference = getBooks(userId)
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns books
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { book.reference } returns bookDocumentReference
            every { batch.delete(bookDocumentReference) } returns mockk()
        }
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenDeleteBooksFailure(
        userId: String,
        batch: WriteBatch,
        books: List<DocumentSnapshot>,
        exception: Exception,
    ) {
        every { firestore.batch() } returns batch
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>(relaxed = true)
        val collectionReference = getBooks(userId)
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns books
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { book.reference } returns bookDocumentReference
            every { batch.delete(bookDocumentReference) } returns mockk()
        }
        val task2 = mockk<Task<Void>>()
        every { batch.commit() } returns task2
        every { task2.isComplete } returns true
        every { task2.isCanceled } returns false
        every { task2.exception } returns exception
    }

    private fun givenRemoteConfigFetchSuccess(key: String, value: String) {
        every { remoteConfig.getString(key) } returns value
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        val task = mockk<Task<Boolean>>()
        every { task.addOnCompleteListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onComplete(
                mockk {
                    every { isSuccessful } returns true
                },
            )
            task
        }
        every { remoteConfig.fetchAndActivate() } returns task
    }

    private fun givenRemoteConfigFetchFailure(key: String, value: String) {
        every { remoteConfig.getString(key) } returns value
        val task = mockk<Task<Boolean>>()
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        every { task.addOnCompleteListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onComplete(
                mockk {
                    every { isSuccessful } returns true
                },
            )
            task
        }
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns RuntimeException("Firestore error")
        every { remoteConfig.fetchAndActivate() } returns task
    }

    private fun givenRemoteConfigGetSuccess(key: String, value: String) {
        every { remoteConfig.fetch(0) } returns Tasks.forResult(mockk<Void>())
        every { remoteConfig.activate() } returns Tasks.forResult(true)
        every { remoteConfig.getString(key) } returns value
    }

    private fun givenRemoteConfigGetFailure(key: String, value: String) {
        val task = mockk<Task<Boolean>>()
        every { remoteConfig.fetch(0) } returns Tasks.forResult(mockk<Void>())
        every { remoteConfig.activate() } returns task
        every { remoteConfig.getString(key) } returns value
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns RuntimeException("Firestore error")
    }
}