/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.frienddetail.FriendDetailUiState
import aragones.sergio.readercollection.presentation.frienddetail.FriendDetailViewModel
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.Constants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FriendDetailViewModelTest {

    private val testUserId = "userId"
    private val testFriendId = "friendId"
    private val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        this["userId"] = testFriendId
    }
    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { userId } returns testUserId
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = FriendDetailViewModel(
        savedStateHandle,
        BooksRepository(
            booksLocalDataSource,
            booksRemoteDataSource,
            ioDispatcher,
        ),
        UserRepository(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN friend with books WHEN fetchFriend THEN return Success state with friend and books data`() =
        runTest {
            val friend = User("friendId", "", RequestStatus.APPROVED)
            val book = Book("bookId")
            coEvery {
                userRemoteDataSource.getFriend(any(), any())
            } returns Result.success(friend.toRemoteData())
            coEvery {
                booksRemoteDataSource.getBooks(any())
            } returns Result.success(listOf(book.toRemoteData()))

            viewModel.state.test {
                assertEquals(FriendDetailUiState.Loading, awaitItem())

                viewModel.fetchFriend()

                assertEquals(
                    FriendDetailUiState.Success(friend, Books(listOf(book))),
                    awaitItem(),
                )
            }
            coVerify { userRemoteDataSource.getFriend(testUserId, testFriendId) }
            coVerify { booksRemoteDataSource.getBooks(testFriendId) }
        }

    @Test
    fun `GIVEN error on get friend WHEN fetchFriend THEN show no friend found error`() = runTest {
        val book = Book("bookId")
        coEvery {
            userRemoteDataSource.getFriend(any(), any())
        } returns Result.failure(NoSuchElementException())
        coEvery {
            booksRemoteDataSource.getBooks(any())
        } returns Result.success(listOf(book.toRemoteData()))

        viewModel.error.test {
            assertEquals(null, awaitItem())

            viewModel.fetchFriend()

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.no_friends_found,
                ),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.getFriend(testUserId, testFriendId) }
        coVerify { booksRemoteDataSource.getBooks(testFriendId) }
    }

    @Test
    fun `GIVEN error on get friend books WHEN fetchFriend THEN show error`() = runTest {
        val friend = User("friendId", "", RequestStatus.APPROVED)
        coEvery {
            userRemoteDataSource.getFriend(any(), any())
        } returns Result.success(friend.toRemoteData())
        coEvery {
            booksRemoteDataSource.getBooks(any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.error.test {
            assertEquals(null, awaitItem())

            viewModel.fetchFriend()

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
                ),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.getFriend(testUserId, testFriendId) }
        coVerify { booksRemoteDataSource.getBooks(testFriendId) }
    }

    @Test
    fun `GIVEN success response WHEN deleteFriend THEN show success message`() = runTest {
        coEvery {
            userRemoteDataSource.deleteFriend(any(), any())
        } returns Result.success(Unit)

        viewModel.infoDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.deleteFriend()

            assertEquals(
                R.string.friend_removed,
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.deleteFriend(testUserId, testFriendId) }
    }

    @Test
    fun `GIVEN failure response WHEN deleteFriend THEN show error`() = runTest {
        coEvery {
            userRemoteDataSource.deleteFriend(any(), any())
        } returns Result.failure(Exception())

        viewModel.error.test {
            assertEquals(null, awaitItem())

            viewModel.deleteFriend()

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_search,
                ),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.deleteFriend(testUserId, testFriendId) }
    }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.user_remove_confirmation)

            assertEquals(R.string.user_remove_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.user_remove_confirmation)
            assertEquals(R.string.user_remove_confirmation, awaitItem())

            viewModel.showConfirmationDialog(R.string.user_remove_confirmation)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            val confirmationDialogMessage = this
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)
            assertEquals(
                R.string.profile_delete_confirmation,
                confirmationDialogMessage.awaitItem(),
            )

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())
                coEvery {
                    userRemoteDataSource.deleteFriend(any(), any())
                } returns Result.success(Unit)
                viewModel.deleteFriend()
                assertEquals(
                    R.string.friend_removed,
                    infoDialogMessage.awaitItem(),
                )

                viewModel.error.test {
                    val profileError = this
                    assertEquals(null, awaitItem())
                    coEvery {
                        userRemoteDataSource.deleteFriend(any(), any())
                    } returns Result.failure(Exception())
                    viewModel.deleteFriend()
                    assertEquals(
                        ErrorModel(
                            Constants.EMPTY_VALUE,
                            R.string.error_search,
                        ),
                        awaitItem(),
                    )

                    viewModel.closeDialogs()

                    assertEquals(-1, confirmationDialogMessage.awaitItem())
                    assertEquals(-1, infoDialogMessage.awaitItem())
                    assertEquals(null, profileError.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.infoDialogMessageId.test {
                assertEquals(-1, awaitItem())

                viewModel.error.test {
                    assertEquals(null, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }
}