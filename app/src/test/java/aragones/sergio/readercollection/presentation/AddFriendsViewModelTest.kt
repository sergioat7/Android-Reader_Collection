/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.addfriend.AddFriendsUiState
import aragones.sergio.readercollection.presentation.addfriend.AddFriendsViewModel
import aragones.sergio.readercollection.presentation.addfriend.UserUi
import aragones.sergio.readercollection.presentation.addfriend.UsersUi
import com.aragones.sergio.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class AddFriendsViewModelTest {

    private val testUserId = "userId"
    private val testUsername = "username"
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { userId } returns testUserId
        every { username } returns testUsername
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = AddFriendsViewModel(
        UserRepository(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN new friend WHEN searchUserWith THEN return searched friend`() = runTest {
        val newFriend = User("userId", "username", RequestStatus.PENDING_FRIEND)
        coEvery {
            userRemoteDataSource.getUser(any(), any())
        } returns Result.success(newFriend.toRemoteData())
        coEvery {
            userRemoteDataSource.getFriends(any())
        } returns Result.success(emptyList())

        viewModel.state.test {
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                ),
                awaitItem(),
            )

            viewModel.searchUserWith(newFriend.username)

            Assert.assertEquals(
                AddFriendsUiState.Loading(newFriend.username),
                awaitItem(),
            )
            val result = awaitItem()
            Assert.assertEquals(
                true,
                result is AddFriendsUiState.Success,
            )
            val user = (result as AddFriendsUiState.Success).users.users.first()
            Assert.assertEquals(
                newFriend.id,
                user.id,
            )
            Assert.assertEquals(
                newFriend.username,
                user.username,
            )
            Assert.assertEquals(
                newFriend.status,
                user.status,
            )
            Assert.assertEquals(
                newFriend.username,
                result.query,
            )
        }
        coVerify { userRemoteDataSource.getUser(newFriend.username, testUserId) }
        coVerify { userRemoteDataSource.getFriends(testUserId) }
    }

    @Test
    fun `GIVEN current friend WHEN searchUserWith THEN return current friend`() = runTest {
        val currentFriend = User("userId", "username", RequestStatus.APPROVED)
        coEvery {
            userRemoteDataSource.getUser(any(), any())
        } returns Result.success(currentFriend.toRemoteData())
        coEvery {
            userRemoteDataSource.getFriends(any())
        } returns Result.success(listOf(currentFriend.toRemoteData()))

        viewModel.state.test {
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                ),
                awaitItem(),
            )

            viewModel.searchUserWith(currentFriend.username)

            Assert.assertEquals(
                AddFriendsUiState.Loading(currentFriend.username),
                awaitItem(),
            )
            val result = awaitItem()
            Assert.assertEquals(
                true,
                result is AddFriendsUiState.Success,
            )
            val user = (result as AddFriendsUiState.Success).users.users.first()
            Assert.assertEquals(
                currentFriend.id,
                user.id,
            )
            Assert.assertEquals(
                currentFriend.username,
                user.username,
            )
            Assert.assertEquals(
                currentFriend.status,
                user.status,
            )
            Assert.assertEquals(
                currentFriend.username,
                result.query,
            )
        }
        coVerify { userRemoteDataSource.getUser(currentFriend.username, testUserId) }
        coVerify { userRemoteDataSource.getFriends(testUserId) }
    }

    @Test
    fun `GIVEN error on get friends request WHEN searchUserWith THEN return searched friend`() =
        runTest {
            val newFriend = User("userId", "username", RequestStatus.PENDING_FRIEND)
            coEvery {
                userRemoteDataSource.getUser(any(), any())
            } returns Result.success(newFriend.toRemoteData())
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.failure(Exception())

            viewModel.state.test {
                Assert.assertEquals(
                    AddFriendsUiState.Success(
                        users = UsersUi(),
                        query = "",
                    ),
                    awaitItem(),
                )

                viewModel.searchUserWith(newFriend.username)

                Assert.assertEquals(
                    AddFriendsUiState.Loading(newFriend.username),
                    awaitItem(),
                )
                val result = awaitItem()
                Assert.assertEquals(
                    true,
                    result is AddFriendsUiState.Success,
                )
                val user = (result as AddFriendsUiState.Success).users.users.first()
                Assert.assertEquals(
                    newFriend.id,
                    user.id,
                )
                Assert.assertEquals(
                    newFriend.username,
                    user.username,
                )
                Assert.assertEquals(
                    newFriend.status,
                    user.status,
                )
                Assert.assertEquals(
                    newFriend.username,
                    result.query,
                )
            }
            coVerify { userRemoteDataSource.getUser(newFriend.username, testUserId) }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
        }

    @Test
    fun `GIVEN no user found error WHEN searchUserWith THEN return empty list`() = runTest {
        val userToSearch = "userToSearch"
        coEvery {
            userRemoteDataSource.getUser(any(), any())
        } returns Result.failure(NoSuchElementException())

        viewModel.state.test {
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                ),
                awaitItem(),
            )

            viewModel.searchUserWith(userToSearch)

            Assert.assertEquals(
                AddFriendsUiState.Loading(userToSearch),
                awaitItem(),
            )
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = userToSearch,
                ),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.getUser(userToSearch, testUserId) }
        coVerify(exactly = 0) { userRemoteDataSource.getFriends(testUserId) }
    }

    @Test
    fun `GIVEN generic error WHEN searchUserWith THEN show error`() = runTest {
        val userToSearch = "userToSearch"
        coEvery {
            userRemoteDataSource.getUser(any(), any())
        } returns Result.failure(Exception())

        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.searchUserWith(userToSearch)

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
                ),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.getUser(userToSearch, testUserId) }
        coVerify(exactly = 0) { userRemoteDataSource.getFriends(testUserId) }
    }

    @Test
    fun `GIVEN empty username WHEN searchUserWith THEN do nothing`() = runTest {
        viewModel.state.test {
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                ),
                awaitItem(),
            )

            viewModel.searchUserWith("")

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN success response WHEN requestFriendship THEN show error`() = runTest {
        val user = User(
            id = testUserId,
            username = testUsername,
            status = RequestStatus.PENDING_MINE,
        )
        val friend = User(
            id = "userId",
            username = "username",
            status = RequestStatus.PENDING_FRIEND,
        )
        val friendUi = UserUi(
            id = friend.id,
            username = friend.username,
            status = friend.status,
            isLoading = false,
        )
        coEvery {
            userRemoteDataSource.requestFriendship(any(), any())
        } returns Result.success(Unit)

        viewModel.state.test {
            Assert.assertEquals(
                AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                ),
                awaitItem(),
            )

            viewModel.requestFriendship(friendUi)

            expectNoEvents()
        }
        coVerify {
            userRemoteDataSource.requestFriendship(
                user.toRemoteData(),
                friend.toRemoteData(),
            )
        }
    }

    @Test
    fun `GIVEN failure response WHEN requestFriendship THEN show error`() = runTest {
        val user = User(
            id = testUserId,
            username = testUsername,
            status = RequestStatus.PENDING_MINE,
        )
        val friend = User(
            id = "userId",
            username = "username",
            status = RequestStatus.PENDING_FRIEND,
        )
        val friendUi = UserUi(
            id = friend.id,
            username = friend.username,
            status = friend.status,
            isLoading = false,
        )
        coEvery {
            userRemoteDataSource.requestFriendship(any(), any())
        } returns Result.failure(Exception())

        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.requestFriendship(friendUi)

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_search,
                ),
                awaitItem(),
            )
        }
        coVerify {
            userRemoteDataSource.requestFriendship(
                user.toRemoteData(),
                friend.toRemoteData(),
            )
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog message is reset`() = runTest {
        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())
            coEvery {
                userRemoteDataSource.getUser(any(), any())
            } returns Result.failure(Exception())
            viewModel.searchUserWith("test")
            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
                ),
                awaitItem(),
            )

            viewModel.closeDialogs()

            Assert.assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.closeDialogs()

            expectNoEvents()
        }
    }
}