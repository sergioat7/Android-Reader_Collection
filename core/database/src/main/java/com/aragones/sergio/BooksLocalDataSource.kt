/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package com.aragones.sergio

import androidx.sqlite.db.SupportSQLiteQuery
import com.aragones.sergio.model.Book
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class BooksLocalDataSource @Inject constructor(
    private val bookDao: BookDao
) {

    //region Private properties
    private val databaseScheduler: Scheduler = Schedulers.io()
    private val mainObserver: Scheduler = AndroidSchedulers.mainThread()
    //endregion

    //region Public methods
    fun getBooks(query: SupportSQLiteQuery): Maybe<List<Book>> {

        return bookDao
            .getBooks(query)
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun getPendingBooks(): Maybe<List<Book>> {

        return bookDao
            .getPendingBooks()
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun importDataFrom(books: List<Book>): Completable {

        return bookDao
            .insertBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun getBook(googleId: String): Single<Book> {

        return bookDao
            .getBook(googleId)
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun insertBooks(books: List<Book>): Completable {

        return bookDao
            .insertBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun updateBooks(books: List<Book>): Completable {

        return bookDao
            .updateBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }

    fun deleteBooks(books: List<Book>): Completable {

        return bookDao
            .deleteBooks(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(databaseScheduler)
            .observeOn(mainObserver)
    }
    //endregion
}