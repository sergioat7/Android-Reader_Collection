/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.domain

import androidx.sqlite.db.SupportSQLiteQuery
import aragones.sergio.readercollection.domain.di.MainDispatcher
import com.aragones.sergio.BookDao
import com.aragones.sergio.ReaderCollectionDatabase
import com.aragones.sergio.model.Book
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import javax.inject.Inject

class BooksLocalDataSource @Inject constructor(
    private val bookDao: BookDao,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) {

    //region Private properties
    private val SUBSCRIBER_SCHEDULER: Scheduler = Schedulers.io()
    private val OBSERVER_SCHEDULER: Scheduler = AndroidSchedulers.mainThread()
    private val externalScope = CoroutineScope(Job() + mainDispatcher)
    //endregion

    //region Public methods
    fun getBooksDatabaseObserver(query: SupportSQLiteQuery): Maybe<List<Book>> {

        return bookDao
            .getBooksObserver(query)
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun getPendingBooksDatabaseObserver(): Maybe<List<Book>> {

        return bookDao
            .getPendingBooksObserver()
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun importDataFrom(books: List<Book>): Completable {

        return bookDao
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun getBookDatabaseObserver(googleId: String): Single<Book> {

        return bookDao
            .getBookObserver(googleId)
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun insertBooksDatabaseObserver(books: List<Book>): Completable {

        return bookDao
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun updateBooksDatabaseObserver(books: List<Book>): Completable {

        return bookDao
            .updateBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun deleteBooksDatabaseObserver(books: List<Book>): Completable {
        
        return bookDao
            .deleteBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }
    //endregion
}