/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.MoshiDateAdapter
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.domain.model.Book
import com.aragones.sergio.BooksLocalDataSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class BooksRepository(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) {

    //region Private properties
    private val moshiAdapter = Moshi
        .Builder()
        .add(MoshiDateAdapter("dd/MM/yyyy"))
        .build()
        .adapter<List<Book?>?>(
            Types.newParameterizedType(
                List::class.java,
                Book::class.java,
            ),
        )
    //endregion

    //region Public methods
    suspend fun loadBooks(uuid: String): Result<Unit> = withContext(ioDispatcher) {
        withTimeout(TIMEOUT) {
            booksRemoteDataSource.getBooks(uuid)
        }.fold(
            onSuccess = { remoteBooks ->
                booksLocalDataSource.insertBooks(
                    remoteBooks.map { it.toDomain().toLocalData() },
                )
                Result.success(Unit)
            },
            onFailure = {
                Result.failure(it)
            },
        )
    }

    suspend fun syncBooks(uuid: String): Result<Unit> = withContext(ioDispatcher) {
        val remoteBooks = withTimeout(TIMEOUT) {
            booksRemoteDataSource.getBooks(uuid)
        }.fold(
            onSuccess = { it },
            onFailure = { emptyList() },
        )
        val localBooks = booksLocalDataSource.getAllBooks().firstOrNull() ?: emptyList()

        val disabledContent = arrayListOf<BookResponse>()
        for (remoteBook in remoteBooks) {
            if (localBooks.firstOrNull { it.id == remoteBook.id } == null) {
                disabledContent.add(remoteBook)
            }
        }
        val currentBooks = localBooks.map {
            it.toDomain().toRemoteData()
        }
        withTimeout(TIMEOUT) {
            booksRemoteDataSource.syncBooks(
                uuid = uuid,
                booksToSave = currentBooks,
                booksToRemove = disabledContent,
            )
        }.fold(
            onSuccess = {
                Result.success(Unit)
            },
            onFailure = {
                Result.failure(it)
            },
        )
    }

    fun getBooks(): Flow<List<Book>> = booksLocalDataSource
        .getAllBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }

    fun getReadBooks(): Flow<List<Book>> = booksLocalDataSource
        .getReadBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }

    suspend fun importDataFrom(jsonData: String): Result<Unit> = runCatching {
        val books = moshiAdapter.fromJson(jsonData)?.mapNotNull { it } ?: listOf()
        booksLocalDataSource
            .importDataFrom(books.map { it.toLocalData() })
    }

    suspend fun exportDataTo(): Result<String> = runCatching {
        val books = booksLocalDataSource
            .getAllBooks()
            .firstOrNull()
            ?.map { it.toDomain() }
            ?: emptyList()
        val json = moshiAdapter.toJson(books)
        return Result.success(json)
    }

    suspend fun getBook(id: String): Result<Pair<Book, Boolean>> = runCatching {
        booksLocalDataSource.getBook(id)
    }.fold(
        onSuccess = { localBook ->
            if (localBook != null) {
                Result.success(localBook.toDomain() to true)
            } else {
                withTimeout(TIMEOUT) {
                    booksRemoteDataSource.getBook(id)
                }.fold(
                    onSuccess = {
                        Result.success(it.toDomain() to false)
                    },
                    onFailure = {
                        Result.failure(it)
                    },
                )
            }
        },
        onFailure = {
            Result.failure(it)
        },
    )

    suspend fun createBook(newBook: Book): Result<Unit> = runCatching {
        booksLocalDataSource
            .insertBooks(listOf(newBook.toLocalData()))
    }

    suspend fun setBook(book: Book): Result<Book> = runCatching {
        booksLocalDataSource.updateBooks(listOf(book.toLocalData()))
        return Result.success(book)
    }

    suspend fun setBooks(books: List<Book>): Result<Unit> = runCatching {
        booksLocalDataSource
            .updateBooks(books.map { it.toLocalData() })
    }

    suspend fun deleteBook(bookId: String): Result<Unit> = runCatching {
        val book = booksLocalDataSource.getBook(bookId)
        if (book != null) {
            booksLocalDataSource.deleteBooks(listOf(book))
        }
        Result.success(Unit)
    }

    suspend fun resetTable(): Result<Unit> = runCatching {
        val books = booksLocalDataSource.getAllBooks().firstOrNull() ?: emptyList()
        booksLocalDataSource.deleteBooks(books)
        Result.success(Unit)
    }

    suspend fun searchBooks(query: String, page: Int, order: String?): Result<List<Book>> =
        withTimeout(TIMEOUT) {
            withContext(ioDispatcher) {
                booksRemoteDataSource.searchBooks(query, page, order)
            }
        }.fold(
            onSuccess = { books ->
                Result.success(books.items?.map { it.toDomain() } ?: listOf())
            },
            onFailure = {
                Result.success(emptyList())
            },
        )

    fun fetchRemoteConfigValues(language: String) =
        booksRemoteDataSource.fetchRemoteConfigValues(language)

    suspend fun getBooksFrom(uuid: String): Result<List<Book>> = withTimeout(TIMEOUT) {
        booksRemoteDataSource.getBooks(uuid)
    }.fold(
        onSuccess = { remoteBooks ->
            Result.success(remoteBooks.map { it.toDomain() })
        },
        onFailure = {
            Result.failure(it)
        },
    )

    suspend fun getFriendBook(friendId: String, bookId: String): Result<Book> =
        withTimeout(TIMEOUT) {
            booksRemoteDataSource.getFriendBook(friendId, bookId)
        }.fold(
            onSuccess = {
                Result.success(it.toDomain())
            },
            onFailure = {
                Result.failure(it)
            },
        )
    //endregion
}

private const val TIMEOUT = 10_000L