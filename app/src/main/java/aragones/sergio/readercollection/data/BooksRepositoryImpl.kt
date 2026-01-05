/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.data

import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.domain.toRemoteData
import com.aragones.sergio.BooksLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json

class BooksRepositoryImpl(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val booksRemoteDataSource: BooksRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : BooksRepository {

    //region Public methods
    override suspend fun loadBooks(uuid: String): Result<Unit> = withContext(ioDispatcher) {
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

    override suspend fun syncBooks(uuid: String): Result<Unit> = withContext(ioDispatcher) {
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

    override fun getBooks(): Flow<List<Book>> = booksLocalDataSource
        .getAllBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }

    override fun getReadBooks(): Flow<List<Book>> = booksLocalDataSource
        .getReadBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }

    override suspend fun importDataFrom(jsonData: String): Result<Unit> = runCatching {
        val books = Json.decodeFromString<List<Book?>>(jsonData).mapNotNull { it }
        booksLocalDataSource
            .importDataFrom(books.map { it.toLocalData() })
    }

    override suspend fun exportDataTo(): Result<String> = runCatching {
        val books = booksLocalDataSource
            .getAllBooks()
            .firstOrNull()
            ?.map { it.toDomain() }
            ?: emptyList()
        val jsonString = Json.encodeToString(books)
        return Result.success(jsonString)
    }

    override suspend fun getBook(id: String): Result<Pair<Book, Boolean>> = runCatching {
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

    override suspend fun createBook(newBook: Book): Result<Unit> = runCatching {
        booksLocalDataSource
            .insertBooks(listOf(newBook.toLocalData()))
    }

    override suspend fun setBook(book: Book): Result<Book> = runCatching {
        booksLocalDataSource.updateBooks(listOf(book.toLocalData()))
        return Result.success(book)
    }

    override suspend fun setBooks(books: List<Book>): Result<Unit> = runCatching {
        booksLocalDataSource
            .updateBooks(books.map { it.toLocalData() })
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> = runCatching {
        val book = booksLocalDataSource.getBook(bookId)
        if (book != null) {
            booksLocalDataSource.deleteBooks(listOf(book))
        }
        Result.success(Unit)
    }

    override suspend fun resetTable(): Result<Unit> = runCatching {
        val books = booksLocalDataSource.getAllBooks().firstOrNull() ?: emptyList()
        booksLocalDataSource.deleteBooks(books)
        Result.success(Unit)
    }

    override suspend fun searchBooks(query: String, page: Int, order: String?): Result<List<Book>> =
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

    override fun fetchRemoteConfigValues(language: String) =
        booksRemoteDataSource.fetchRemoteConfigValues(language)

    override suspend fun getBooksFrom(uuid: String): Result<List<Book>> = withTimeout(TIMEOUT) {
        booksRemoteDataSource.getBooks(uuid)
    }.fold(
        onSuccess = { remoteBooks ->
            Result.success(remoteBooks.map { it.toDomain() })
        },
        onFailure = {
            Result.failure(it)
        },
    )

    override suspend fun getFriendBook(friendId: String, bookId: String): Result<Book> =
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