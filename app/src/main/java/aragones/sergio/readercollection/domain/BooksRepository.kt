/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.MoshiDateAdapter
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.base.BaseRepository
import aragones.sergio.readercollection.domain.model.Book
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val booksRemoteDataSource: BooksRemoteDataSource,
) : BaseRepository() {

    //region Private properties
    private val ioScheduler: Scheduler = Schedulers.io()
    private val mainScheduler: Scheduler = AndroidSchedulers.mainThread()
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
    fun loadBooks(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
//        booksRemoteDataSource.loadBooks(success, failure)
    }

    fun getBooks(): Flowable<List<Book>> = booksLocalDataSource
        .getAllBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }
        .subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getPendingBooks(): Flowable<List<Book>> = booksLocalDataSource
        .getPendingBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }
        .subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getReadBooks(): Flowable<List<Book>> = booksLocalDataSource
        .getReadBooks()
        .distinctUntilChanged()
        .map { it.map { book -> book.toDomain() } }
        .subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun importDataFrom(jsonData: String): Completable {
        val books = moshiAdapter.fromJson(jsonData)?.mapNotNull { it } ?: listOf()
        return booksLocalDataSource
            .importDataFrom(books.map { it.toLocalData() })
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
    }

    fun exportDataTo(): Single<String> = Single
        .create<String> { emitter ->
            booksLocalDataSource
                .getAllBooks()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        emitter.onSuccess("")
                    },
                    onNext = {
                        val books = it.map { book -> book.toDomain() }
                        emitter.onSuccess(moshiAdapter.toJson(books))
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getBook(googleId: String): Single<Book> = booksLocalDataSource
        .getBook(googleId)
        .subscribeOn(ioScheduler)
        .observeOn(mainScheduler)
        .map { it.toDomain() }

    fun createBook(newBook: Book, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        booksRemoteDataSource.createBook(newBook, success, failure)

        booksLocalDataSource
            .insertBooks(listOf(newBook.toLocalData()))
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    success()
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database,
                        ),
                    )
                },
            ).addTo(disposables)
    }

    fun setBook(book: Book, success: (Book) -> Unit, failure: (ErrorResponse) -> Unit) {
//        booksRemoteDataSource.setBook(book, success = {
        booksLocalDataSource
            .updateBooks(listOf(book.toLocalData()))
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    success(book)
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database,
                        ),
                    )
                },
            ).addTo(disposables)
//        }, failure = failure)
    }

    fun setBooks(books: List<Book>, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        booksRemoteDataSource.setBook(book, success = {
        booksLocalDataSource
            .updateBooks(books.map { it.toLocalData() })
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = {
                    success()
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database,
                        ),
                    )
                },
            ).addTo(disposables)
//        }, failure = failure)
    }

    fun setFavouriteBook(
        bookId: String,
        isFavourite: Boolean,
        success: (Book) -> Unit,
        failure: (ErrorResponse) -> Unit,
    ) {
//        booksRemoteDataSource.setFavouriteBook(bookId = , success = { book ->
//            booksLocalDataSource.updateBooks(listOf(book)).subscribeBy(
//                onComplete = {
//                    success(book)
//                },
//                onError = {
//                    failure(
//                        ErrorResponse(
//                            Constants.EMPTY_VALUE,
//                            R.string.error_database
//                        )
//                    )
//                }
//            ).addTo(disposables)
//        }, failure)
        booksLocalDataSource
            .getBook(bookId)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = {
                    val book = it.toDomain()
                    book.isFavourite = isFavourite
                    booksLocalDataSource
                        .updateBooks(listOf(book.toLocalData()))
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onComplete = {
                                success(book)
                            },
                            onError = {
                                failure(
                                    ErrorResponse(
                                        Constants.EMPTY_VALUE,
                                        R.string.error_database,
                                    ),
                                )
                            },
                        ).addTo(disposables)
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database,
                        ),
                    )
                },
            ).addTo(disposables)
    }

    fun deleteBook(bookId: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        booksRemoteDataSource.deleteBook(bookId, success = {
        booksLocalDataSource
            .getBook(bookId)
            .subscribeOn(ioScheduler)
            .subscribeBy(
                onSuccess = { book ->
                    booksLocalDataSource
                        .deleteBooks(listOf(book))
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .subscribeBy(
                            onComplete = {
                                success()
                            },
                            onError = {
                                failure(
                                    ErrorResponse(
                                        Constants.EMPTY_VALUE,
                                        R.string.error_database,
                                    ),
                                )
                            },
                        ).addTo(disposables)
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database,
                        ),
                    )
                },
            ).addTo(disposables)
//        }, failure)
    }

    fun resetTable(): Completable = Completable
        .create { emitter ->

            booksLocalDataSource
                .getAllBooks()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        emitter.onComplete()
                    },
                    onNext = { books ->
                        booksLocalDataSource
                            .deleteBooks(books)
                            .subscribeOn(ioScheduler)
                            .observeOn(mainScheduler)
                            .subscribeBy(
                                onComplete = {
                                    emitter.onComplete()
                                },
                                onError = {
                                    emitter.onError(it)
                                },
                            ).addTo(disposables)
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun searchBooks(query: String, page: Int, order: String?): Single<List<Book>> =
        booksRemoteDataSource
            .searchBooks(query, page, order)
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .map { it.items?.map { book -> book.toDomain() } ?: listOf() }

    fun getRemoteBook(volumeId: String): Single<Book> = booksRemoteDataSource
        .getBook(volumeId)
        .subscribeOn(ioScheduler)
        .observeOn(mainScheduler)
        .map { it.toDomain() }

    fun fetchRemoteConfigValues(language: String) =
        booksRemoteDataSource.fetchRemoteConfigValues(language)
    //endregion
}