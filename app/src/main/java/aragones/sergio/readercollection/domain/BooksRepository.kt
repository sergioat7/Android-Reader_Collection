/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.domain

import androidx.sqlite.db.SimpleSQLiteQuery
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
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val booksRemoteDataSource: BooksRemoteDataSource
) : BaseRepository() {

    //region Private properties
    private val SUBSCRIBER_SCHEDULER: Scheduler = Schedulers.io()
    private val OBSERVER_SCHEDULER: Scheduler = AndroidSchedulers.mainThread()
    private val moshiAdapter = Moshi.Builder()
        .add(MoshiDateAdapter("MMM dd, yyyy"))
        .build().adapter<List<Book?>?>(
            Types.newParameterizedType(
                List::class.java,
                Book::class.java
            )
        )
    //endregion

    //region Public methods
    fun loadBooks(success: () -> Unit, failure: (ErrorResponse) -> Unit) {
        success()
//        booksRemoteDataSource.loadBooks(success, failure)
    }

    fun getBooksDatabaseObserver(
        format: String? = null,
        state: String? = null,
        isFavourite: Boolean? = null,
        sortParam: String? = null
    ): Maybe<List<Book>> {

        var queryString = "SELECT * FROM Book"
        var queryConditions = ""
        format?.let {
            queryConditions += "format == '${it}' AND "
        }
        state?.let {
            queryConditions += "state == '${it}' AND "
        }
        isFavourite?.let {
            queryConditions += if (it) "isFavourite == '1' AND " else "isFavourite == '0' AND "
        }

        if (queryConditions.isNotBlank()) queryString += " WHERE " + queryConditions.dropLast(5)

        sortParam?.let {
            queryString += " ORDER BY $it"
        }

        val query = SimpleSQLiteQuery(queryString)
        return booksLocalDataSource
            .getBooksDatabaseObserver(query)
            .map { it.map { book -> book.toDomain() } }
    }

    fun getPendingBooksDatabaseObserver(): Maybe<List<Book>> {

        return booksLocalDataSource
            .getPendingBooksDatabaseObserver()
            .map { it.map { book -> book.toDomain() } }
    }

    fun importDataFrom(jsonData: String): Completable {

        val books = moshiAdapter.fromJson(jsonData)?.mapNotNull { it } ?: listOf()
        return booksLocalDataSource.importDataFrom(books.map { it.toLocalData() })
    }

    fun exportDataTo(): Single<String> {

        return Single.create<String> { emitter ->
            booksLocalDataSource.getBooksDatabaseObserver(SimpleSQLiteQuery("SELECT * FROM Book"))
                .subscribeBy(
                    onComplete = {
                        emitter.onSuccess("")
                    },
                    onSuccess = {

                        val books = it.map { book -> book.toDomain() }
                        emitter.onSuccess(moshiAdapter.toJson(books))
                    },
                    onError = {
                        emitter.onError(it)
                    }
                ).addTo(disposables)
        }
            .subscribeOn(SUBSCRIBER_SCHEDULER)
            .observeOn(OBSERVER_SCHEDULER)
    }

    fun getBookDatabaseObserver(googleId: String): Single<Book> {

        return booksLocalDataSource
            .getBookDatabaseObserver(googleId)
            .map { it.toDomain() }
    }

    fun createBook(newBook: Book, success: () -> Unit, failure: (ErrorResponse) -> Unit) {
//        booksRemoteDataSource.createBook(newBook, success, failure)

        booksLocalDataSource.insertBooksDatabaseObserver(listOf(newBook.toLocalData())).subscribeBy(
            onComplete = {
                success()
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
    }

    fun setBook(
        book: Book,
        success: (Book) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
//        booksRemoteDataSource.setBook(book, success = {
        booksLocalDataSource.updateBooksDatabaseObserver(listOf(book.toLocalData())).subscribeBy(
            onComplete = {
                success(book)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        }, failure = failure)
    }

    fun setBooks(
        books: List<Book>,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
//        booksRemoteDataSource.setBook(book, success = {
        booksLocalDataSource.updateBooksDatabaseObserver(books.map { it.toLocalData() })
            .subscribeBy(
                onComplete = {
                    success()
                },
                onError = {
                    failure(
                        ErrorResponse(
                            Constants.EMPTY_VALUE,
                            R.string.error_database
                        )
                    )
                }
            ).addTo(disposables)
//        }, failure = failure)
    }

    fun setFavouriteBook(
        bookId: String,
        isFavourite: Boolean,
        success: (Book) -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
//        booksRemoteDataSource.setFavouriteBook(bookId = , success = { book ->
//            booksLocalDataSource.updateBooksDatabaseObserver(listOf(book)).subscribeBy(
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
        booksLocalDataSource.getBookDatabaseObserver(bookId).subscribeBy(
            onSuccess = {

                val book = it.toDomain()
                book.isFavourite = isFavourite
                booksLocalDataSource.updateBooksDatabaseObserver(listOf(book.toLocalData()))
                    .subscribeBy(
                        onComplete = {
                            success(book)
                        },
                        onError = {
                            failure(
                                ErrorResponse(
                                    Constants.EMPTY_VALUE,
                                    R.string.error_database
                                )
                            )
                        }
                    ).addTo(disposables)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
    }

    fun deleteBook(bookId: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {

//        booksRemoteDataSource.deleteBook(bookId, success = {
        booksLocalDataSource.getBookDatabaseObserver(bookId).subscribeBy(
            onSuccess = { book ->
                booksLocalDataSource.deleteBooksDatabaseObserver(listOf(book)).subscribeBy(
                    onComplete = {
                        success()
                    },
                    onError = {
                        failure(
                            ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_database
                            )
                        )
                    }
                ).addTo(disposables)
            },
            onError = {
                failure(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database
                    )
                )
            }
        ).addTo(disposables)
//        }, failure)
    }

    fun resetTableObserver(): Completable {
        return Completable.create { emitter ->

            booksLocalDataSource.getBooksDatabaseObserver(SimpleSQLiteQuery("SELECT * FROM Book"))
                .subscribeBy(
                    onComplete = {
                        emitter.onComplete()
                    },
                    onSuccess = { books ->
                        booksLocalDataSource.deleteBooksDatabaseObserver(books).subscribeBy(
                            onComplete = {
                                emitter.onComplete()
                            },
                            onError = {
                                emitter.onError(it)
                            }
                        ).addTo(disposables)
                    },
                    onError = {
                        emitter.onError(it)
                    }
                ).addTo(disposables)
        }.subscribeOn(SUBSCRIBER_SCHEDULER).observeOn(OBSERVER_SCHEDULER)
    }

    fun searchBooksObserver(
        query: String,
        page: Int,
        order: String?
    ): Single<List<Book>> {
        return Single.create { emitter ->

            booksRemoteDataSource.searchBooksObserver(query, page, order)
                .subscribeBy(onSuccess = {

                    val values = it.items?.map { book -> book.toDomain() } ?: listOf()
                    emitter.onSuccess(values)
                }, onError = {
                    emitter.onError(it)
                }).addTo(disposables)
        }.subscribeOn(SUBSCRIBER_SCHEDULER).observeOn(OBSERVER_SCHEDULER)
    }

    fun getBookObserver(volumeId: String): Single<Book> {
        return Single.create { emitter ->

            booksRemoteDataSource.getBookObserver(volumeId)
                .subscribeBy(onSuccess = {
                    emitter.onSuccess(it.toDomain())
                }, onError = {
                    emitter.onError(it)
                }).addTo(disposables)
        }.subscribeOn(SUBSCRIBER_SCHEDULER).observeOn(OBSERVER_SCHEDULER)
    }
    //endregion
}