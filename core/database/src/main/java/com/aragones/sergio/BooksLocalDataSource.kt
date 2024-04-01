/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio

import androidx.sqlite.db.SupportSQLiteQuery
import com.aragones.sergio.model.Book
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BooksLocalDataSource @Inject constructor(
    private val bookDao: BookDao
) {

    //region Public methods
    fun getBooks(query: SupportSQLiteQuery): Flowable<List<Book>> {

        return bookDao
            .getBooks(query)
            .`as`(RxJavaBridge.toV3Flowable())
    }

    fun getPendingBooks(): Flowable<List<Book>> {

        return bookDao
            .getPendingBooks()
            .`as`(RxJavaBridge.toV3Flowable())
    }

    fun getReadBooks(): Flowable<List<Book>> {

        return bookDao
            .getReadBooks()
            .`as`(RxJavaBridge.toV3Flowable())
    }

    fun importDataFrom(books: List<Book>): Completable {

        return bookDao
            .insertBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
    }

    fun getBook(googleId: String): Single<Book> {

        return bookDao
            .getBook(googleId)
            .`as`(RxJavaBridge.toV3Single())
    }

    fun insertBooks(books: List<Book>): Completable {

        return bookDao
            .insertBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
    }

    fun updateBooks(books: List<Book>): Completable {

        return bookDao
            .updateBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
    }

    fun deleteBooks(books: List<Book>): Completable {

        return bookDao
            .deleteBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
    }
    //endregion
}