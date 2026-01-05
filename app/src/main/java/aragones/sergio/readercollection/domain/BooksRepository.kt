/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BooksRepository {
    suspend fun loadBooks(uuid: String): Result<Unit>
    suspend fun syncBooks(uuid: String): Result<Unit>
    fun getBooks(): Flow<List<Book>>
    fun getReadBooks(): Flow<List<Book>>
    suspend fun importDataFrom(jsonData: String): Result<Unit>
    suspend fun exportDataTo(): Result<String>
    suspend fun getBook(id: String): Result<Pair<Book, Boolean>>
    suspend fun createBook(newBook: Book): Result<Unit>
    suspend fun setBook(book: Book): Result<Book>
    suspend fun setBooks(books: List<Book>): Result<Unit>
    suspend fun deleteBook(bookId: String): Result<Unit>
    suspend fun resetTable(): Result<Unit>
    suspend fun searchBooks(query: String, page: Int, order: String?): Result<List<Book>>
    fun fetchRemoteConfigValues(language: String)
    suspend fun getBooksFrom(uuid: String): Result<List<Book>>
    suspend fun getFriendBook(friendId: String, bookId: String): Result<Book>
}
