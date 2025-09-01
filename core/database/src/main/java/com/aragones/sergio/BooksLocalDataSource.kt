/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio

import com.aragones.sergio.model.Book
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class BooksLocalDataSource @Inject constructor(
    private val bookDao: BookDao,
) {

    //region Public methods
    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    fun getReadBooks(): Flow<List<Book>> = bookDao.getReadBooks()

    suspend fun importDataFrom(books: List<Book>) = bookDao.insertBooks(books)

    suspend fun getBook(googleId: String): Book = bookDao.getBook(googleId)

    suspend fun insertBooks(books: List<Book>) = bookDao.insertBooks(books)

    suspend fun updateBooks(books: List<Book>) = bookDao.updateBooks(books)

    suspend fun deleteBooks(books: List<Book>) = bookDao.deleteBooks(books)
    //endregion
}