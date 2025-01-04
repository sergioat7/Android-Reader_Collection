/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio

import com.aragones.sergio.model.Book
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BooksLocalDataSource @Inject constructor(
    private val bookDao: BookDao,
) {

    //region Public methods
    fun getAllBooks(): Flowable<List<Book>> = bookDao
        .getAllBooks()
        .`as`(RxJavaBridge.toV3Flowable())

    fun getReadBooks(): Flowable<List<Book>> = bookDao
        .getReadBooks()
        .`as`(RxJavaBridge.toV3Flowable())

    fun importDataFrom(books: List<Book>): Completable = bookDao
        .insertBooks(books)
        .`as`(RxJavaBridge.toV3Completable())

    fun getBook(googleId: String): Single<Book> = bookDao
        .getBook(googleId)
        .`as`(RxJavaBridge.toV3Single())

    fun insertBooks(books: List<Book>): Completable = bookDao
        .insertBooks(books)
        .`as`(RxJavaBridge.toV3Completable())

    fun updateBooks(books: List<Book>): Completable = bookDao
        .updateBooks(books)
        .`as`(RxJavaBridge.toV3Completable())

    fun deleteBooks(books: List<Book>): Completable = bookDao
        .deleteBooks(books)
        .`as`(RxJavaBridge.toV3Completable())
    //endregion
}