/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/10/2025
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
import aragones.sergio.readercollection.domain.model.Users
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.friends.FriendsUiState
import aragones.sergio.readercollection.presentation.friends.FriendsViewModel
import com.aragones.sergio.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class FriendsViewModelTest {

    private val testUserId = "userId"
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { userId } returns testUserId
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = FriendsViewModel(
        UserRepository(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN friends WHEN fetchFriends THEN returns Success state with friends list`() = runTest {
        val friend = User("friendId", "", RequestStatus.APPROVED)
        coEvery {
            userRemoteDataSource.getFriends(any())
        } returns Result.success(listOf(friend.toRemoteData()))

        viewModel.state.test {
            Assert.assertEquals(FriendsUiState.Loading, awaitItem())

            viewModel.fetchFriends()

            Assert.assertEquals(
                FriendsUiState.Success(Users(listOf(friend))),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.getFriends(testUserId) }
        confirmVerified(userRemoteDataSource)
    }

    @Test
    fun `GIVEN no friends WHEN fetchFriends THEN returns Success state with empty list`() =
        runTest {
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.success(emptyList())

            viewModel.state.test {
                Assert.assertEquals(FriendsUiState.Loading, awaitItem())

                viewModel.fetchFriends()

                Assert.assertEquals(
                    FriendsUiState.Success(Users()),
                    awaitItem(),
                )
            }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
            confirmVerified(userRemoteDataSource)
        }

    @Test
    fun `GIVEN error on getting friends WHEN fetchFriends THEN returns Success state with empty list`() =
        runTest {
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.failure(RuntimeException("Firestore error"))

            viewModel.state.test {
                Assert.assertEquals(FriendsUiState.Loading, awaitItem())

                viewModel.fetchFriends()

                Assert.assertEquals(
                    FriendsUiState.Success(Users()),
                    awaitItem(),
                )
            }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
            confirmVerified(userRemoteDataSource)
        }

    @Test
    fun `GIVEN friend id and success response WHEN acceptFriendRequest THEN success message is shown and friend status is updated to Approved`() =
        runTest {
            val friendId = "friendId"
            val friend = User(friendId, "", RequestStatus.PENDING_MINE)
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.success(listOf(friend.toRemoteData()))
            coEvery {
                userRemoteDataSource.acceptFriendRequest(any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                Assert.assertEquals(-1, awaitItem())

                viewModel.state.test {
                    val state = this
                    Assert.assertEquals(FriendsUiState.Loading, awaitItem())
                    viewModel.fetchFriends()
                    Assert.assertEquals(
                        FriendsUiState.Success(Users(listOf(friend))),
                        awaitItem(),
                    )

                    viewModel.acceptFriendRequest(friendId)

                    Assert.assertEquals(
                        R.string.friend_action_successfully_done,
                        infoDialogMessage.awaitItem(),
                    )
                    Assert.assertEquals(
                        FriendsUiState.Success(
                            Users(listOf(friend.copy(status = RequestStatus.APPROVED))),
                        ),
                        state.awaitItem(),
                    )
                }
            }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
            coVerify { userRemoteDataSource.acceptFriendRequest(testUserId, friendId) }
            confirmVerified(userRemoteDataSource)
        }

    @Test
    fun `GIVEN failure response WHEN acceptFriendRequest THEN error is shown`() = runTest {
        val friendId = "friendId"
        coEvery {
            userRemoteDataSource.acceptFriendRequest(any(), any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.acceptFriendRequest(friendId)

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                ),
                awaitItem(),
            )
        }

        coVerify { userRemoteDataSource.acceptFriendRequest(testUserId, friendId) }
        confirmVerified(userRemoteDataSource)
    }

    @Test
    fun `GIVEN friend id and success response WHEN rejectFriendRequest THEN success message is shown and friend is removed from list`() =
        runTest {
            val friendId = "friendId"
            val friend = User(friendId, "", RequestStatus.APPROVED)
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.success(listOf(friend.toRemoteData()))
            coEvery {
                userRemoteDataSource.rejectFriendRequest(any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                Assert.assertEquals(-1, awaitItem())

                viewModel.state.test {
                    val state = this
                    Assert.assertEquals(FriendsUiState.Loading, awaitItem())
                    viewModel.fetchFriends()
                    Assert.assertEquals(
                        FriendsUiState.Success(Users(listOf(friend))),
                        awaitItem(),
                    )

                    viewModel.rejectFriendRequest(friendId)

                    Assert.assertEquals(
                        R.string.friend_action_successfully_done,
                        infoDialogMessage.awaitItem(),
                    )
                    Assert.assertEquals(
                        FriendsUiState.Success(Users()),
                        state.awaitItem(),
                    )
                }
            }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
            coVerify { userRemoteDataSource.rejectFriendRequest(testUserId, friendId) }
            confirmVerified(userRemoteDataSource)
        }

    @Test
    fun `GIVEN failure response WHEN rejectFriendRequest THEN error is shown`() = runTest {
        val friendId = "friendId"
        coEvery {
            userRemoteDataSource.rejectFriendRequest(any(), any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.rejectFriendRequest(friendId)

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                ),
                awaitItem(),
            )
        }

        coVerify { userRemoteDataSource.rejectFriendRequest(testUserId, friendId) }
        confirmVerified(userRemoteDataSource)
    }

    @Test
    fun `GIVEN success response WHEN deleteFriend THEN success message is shown and friend is removed from list`() =
        runTest {
            val friendId = "friendId"
            val friend = User(friendId, "", RequestStatus.APPROVED)
            coEvery {
                userRemoteDataSource.getFriends(any())
            } returns Result.success(listOf(friend.toRemoteData()))
            coEvery {
                userRemoteDataSource.deleteFriend(any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                Assert.assertEquals(-1, awaitItem())

                viewModel.state.test {
                    val state = this
                    Assert.assertEquals(FriendsUiState.Loading, awaitItem())
                    viewModel.fetchFriends()
                    Assert.assertEquals(
                        FriendsUiState.Success(Users(listOf(friend))),
                        awaitItem(),
                    )

                    viewModel.deleteFriend(friendId)

                    Assert.assertEquals(
                        R.string.friend_action_successfully_done,
                        infoDialogMessage.awaitItem(),
                    )
                    Assert.assertEquals(
                        FriendsUiState.Success(Users()),
                        state.awaitItem(),
                    )
                }
            }
            coVerify { userRemoteDataSource.getFriends(testUserId) }
            coVerify { userRemoteDataSource.deleteFriend(testUserId, friendId) }
            confirmVerified(userRemoteDataSource)
        }

    @Test
    fun `GIVEN failure response WHEN deleteFriend THEN error is shown`() = runTest {
        val friendId = "friendId"
        coEvery {
            userRemoteDataSource.deleteFriend(any(), any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.error.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.deleteFriend(friendId)

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                ),
                awaitItem(),
            )
        }

        coVerify { userRemoteDataSource.deleteFriend(testUserId, friendId) }
        confirmVerified(userRemoteDataSource)
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        viewModel.infoDialogMessageId.test {
            val infoDialogMessage = this
            Assert.assertEquals(-1, awaitItem())

            coEvery {
                userRemoteDataSource.acceptFriendRequest(any(), any())
            } returns Result.success(Unit)
            viewModel.acceptFriendRequest("")
            Assert.assertEquals(
                R.string.friend_action_successfully_done,
                awaitItem(),
            )

            viewModel.error.test {
                val error = this
                Assert.assertEquals(null, awaitItem())

                coEvery {
                    userRemoteDataSource.rejectFriendRequest(any(), any())
                } returns Result.failure(RuntimeException("Firestore error"))
                viewModel.rejectFriendRequest("")
                Assert.assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.friend_action_failure,
                    ),
                    awaitItem(),
                )

                viewModel.closeDialogs()

                Assert.assertEquals(-1, infoDialogMessage.awaitItem())
                Assert.assertEquals(null, error.awaitItem())
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.error.test {
                Assert.assertEquals(null, awaitItem())

                viewModel.closeDialogs()

                expectNoEvents()
            }
        }
    }
}