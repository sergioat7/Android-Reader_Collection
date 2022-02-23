/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.repositories

import androidx.sqlite.db.SimpleSQLiteQuery
import aragones.sergio.readercollection.base.BaseRepository
import aragones.sergio.readercollection.models.requests.FavouriteBook
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.BookApiService
import aragones.sergio.readercollection.persistence.AppDatabase
import aragones.sergio.readercollection.utils.CsvWriter
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val api: BookApiService,
    private val database: AppDatabase
) : BaseRepository() {

    //region Public methods
    fun loadBooksObserver(): Completable {

        return Completable.create { emitter ->

            api.getBooks()
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onComplete = {
                        handleDisabledContentObserver(listOf()).subscribeBy(
                            onComplete = {
                                emitter.onComplete()
                            },
                            onError = {
                                emitter.onError(it)
                            }
                        ).addTo(disposables)
                    },
                    onSuccess = { newBooks ->
                        insertBooksDatabaseObserver(newBooks).subscribeBy(
                            onComplete = {
                                handleDisabledContentObserver(newBooks).subscribeBy(
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
                    },
                    onError = {
                        emitter.onError(it)
                    }
                ).addTo(disposables)
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun getBooksDatabaseObserver(
        format: String? = null,
        state: String? = null,
        isFavourite: Boolean? = null,
        sortParam: String? = null
    ): Maybe<List<BookResponse>> {

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
        return database
            .bookDao()
            .getBooksObserver(query)
            .`as`(RxJavaBridge.toV3Maybe())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun exportDataTo(file: File): Boolean {

        return try {
            if (file.createNewFile()) {
                val csvWriter = CsvWriter(FileWriter(file))
                val cursor = database.query("SELECT * FROM Book", null)
                csvWriter.writeNext(cursor.columnNames)
                while (cursor.moveToNext()) {
                    val arrStr = arrayOfNulls<String>(cursor.columnCount)
                    for (i in 0 until cursor.columnCount - 1) arrStr[i] = cursor.getString(i)
                    csvWriter.writeNext(arrStr)
                }
                csvWriter.close()
                cursor.close()
                true
            } else {
                file.delete()
                false
            }
        } catch (e: Exception) {
            file.delete()
            false
        }
    }

    fun getBookDatabaseObserver(googleId: String): Single<BookResponse> {

        return database
            .bookDao()
            .getBookObserver(googleId)
            .`as`(RxJavaBridge.toV3Single())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun createBookObserver(book: BookResponse): Completable {

        return Completable.create { emitter ->

            api.createBook(book)
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onComplete = {

                        loadBooksObserver().subscribeBy(
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
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun updateBookObserver(book: BookResponse): Single<BookResponse> {

        val observer: Single<BookResponse> = Single.create { emitter ->

            api.setBook(book.id, book)
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onSuccess = {

                        updateBooksDatabaseObserver(listOf(book)).subscribeBy(
                            onComplete = {
                                emitter.onSuccess(book)
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
        }
        observer.subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)

        return observer
    }

    fun deleteBookObserver(googleId: String): Completable {

        return Completable.create { emitter ->

            api.deleteBook(googleId)
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onComplete = {
                        getBookDatabaseObserver(googleId).subscribeBy(
                            onSuccess = { book ->
                                deleteBooksDatabaseObserver(listOf(book)).subscribeBy(
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
                    },
                    onError = {
                        emitter.onError(it)
                    }
                ).addTo(disposables)
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun setFavouriteBookObserver(googleId: String, isFavourite: Boolean): Single<BookResponse> {

        val observer: Single<BookResponse> = Single.create { emitter ->

            api.setFavouriteBook(googleId, FavouriteBook(isFavourite))
                .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
                .observeOn(ApiManager.OBSERVER_SCHEDULER)
                .subscribeBy(
                    onSuccess = { book ->
                        updateBooksDatabaseObserver(listOf(book)).subscribeBy(
                            onComplete = {
                                emitter.onSuccess(book)
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
        }
        observer.subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)

        return observer
    }

    fun resetTableObserver(): Completable {

        return Completable.create { emitter ->

            getBooksDatabaseObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onSuccess = { books ->
                    deleteBooksDatabaseObserver(books).subscribeBy(
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
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion

    //region Private methods
    private fun insertBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .insertBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun updateBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .updateBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun deleteBooksDatabaseObserver(books: List<BookResponse>): Completable {
        return database
            .bookDao()
            .deleteBooksObserver(books)
            .`as`(RxJavaBridge.toV3Completable())
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    private fun handleDisabledContentObserver(newBooks: List<BookResponse>): Completable {
        return Completable.create { emitter ->

            getBooksDatabaseObserver().subscribeBy(
                onComplete = {
                    emitter.onComplete()
                },
                onSuccess = { currentBooks ->

                    val booksToRemove = AppDatabase.getDisabledContent(
                        currentBooks,
                        newBooks
                    ) as List<BookResponse>
                    deleteBooksDatabaseObserver(booksToRemove).subscribeBy(
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
        }
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }
    //endregion
}