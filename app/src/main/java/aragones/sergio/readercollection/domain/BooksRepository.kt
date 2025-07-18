/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.MoshiDateAdapter
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.domain.base.BaseRepository
import aragones.sergio.readercollection.domain.di.IoScheduler
import aragones.sergio.readercollection.domain.di.MainScheduler
import aragones.sergio.readercollection.domain.model.Book
import com.aragones.sergio.BooksLocalDataSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BooksRepository @Inject constructor(
    private val booksLocalDataSource: BooksLocalDataSource,
    private val booksRemoteDataSource: BooksRemoteDataSource,
    @IoScheduler private val ioScheduler: Scheduler,
    @MainScheduler private val mainScheduler: Scheduler,
) : BaseRepository() {

    //region Private properties
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
    fun loadBooks(uuid: String): Completable = Completable
        .create { emitter ->
            booksRemoteDataSource
                .getBooks(uuid)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { remoteBooks ->
                        booksLocalDataSource
                            .insertBooks(remoteBooks.map { it.toDomain().toLocalData() })
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

    fun syncBooks(uuid: String): Completable = Completable
        .create { emitter ->
            booksRemoteDataSource
                .getBooks(uuid)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .onErrorReturnItem(emptyList())
                .subscribe { remoteBooks ->
                    booksLocalDataSource
                        .getAllBooks()
                        .firstOrError()
                        .subscribeOn(ioScheduler)
                        .observeOn(mainScheduler)
                        .onErrorReturnItem(emptyList())
                        .subscribeBy(
                            onSuccess = { localBooks ->
                                val disabledContent = arrayListOf<BookResponse>()
                                for (remoteBook in remoteBooks) {
                                    if (localBooks.firstOrNull { it.id == remoteBook.id } == null) {
                                        disabledContent.add(remoteBook)
                                    }
                                }
                                val currentBooks = localBooks.map { it.toDomain().toRemoteData() }
                                booksRemoteDataSource
                                    .syncBooks(
                                        uuid = uuid,
                                        booksToSave = currentBooks,
                                        booksToRemove = disabledContent,
                                    ).timeout(10, TimeUnit.SECONDS)
                                    .subscribeOn(ioScheduler)
                                    .observeOn(mainScheduler)
                                    .subscribeBy(onComplete = {
                                        emitter.onComplete()
                                    }, onError = {
                                        emitter.onError(it)
                                    })
                                    .addTo(disposables)
                            },
                        ).addTo(disposables)
                }.addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getBooks(): Flowable<List<Book>> = booksLocalDataSource
        .getAllBooks()
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
                .firstOrError()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        val books = it.map { book -> book.toDomain() }
                        emitter.onSuccess(moshiAdapter.toJson(books))
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getBook(id: String): Single<Pair<Book, Boolean>> = Single
        .create { emitter ->
            booksLocalDataSource
                .getBook(id)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        emitter.onSuccess(it.toDomain() to true)
                    },
                    onError = {
                        booksRemoteDataSource
                            .getBook(id)
                            .subscribeOn(ioScheduler)
                            .observeOn(mainScheduler)
                            .subscribeBy(
                                onSuccess = {
                                    emitter.onSuccess(it.toDomain() to false)
                                },
                                onError = {
                                    emitter.onError(it)
                                },
                            ).addTo(disposables)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun createBook(newBook: Book): Completable = Completable
        .create { emitter ->
            booksLocalDataSource
                .insertBooks(listOf(newBook.toLocalData()))
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
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun setBook(book: Book): Single<Book> = Single
        .create { emitter ->
            booksLocalDataSource
                .updateBooks(listOf(book.toLocalData()))
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onComplete = {
                        emitter.onSuccess(book)
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun setBooks(books: List<Book>): Completable = Completable
        .create { emitter ->
            booksLocalDataSource
                .updateBooks(books.map { it.toLocalData() })
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
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun deleteBook(bookId: String): Completable = Completable
        .create { emitter ->
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

    fun resetTable(): Completable = Completable
        .create { emitter ->
            booksLocalDataSource
                .getAllBooks()
                .firstOrError()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { books ->
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

    fun fetchRemoteConfigValues(language: String) =
        booksRemoteDataSource.fetchRemoteConfigValues(language)

    fun getBooksFrom(uuid: String): Single<List<Book>> = Single
        .create { emitter ->
            booksRemoteDataSource
                .getBooks(uuid)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { remoteBooks ->
                        emitter.onSuccess(remoteBooks.map { it.toDomain() })
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)

    fun getFriendBook(friendId: String, bookId: String): Single<Book> = Single
        .create { emitter ->
            booksRemoteDataSource
                .getFriendBook(friendId, bookId)
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        emitter.onSuccess(it.toDomain())
                    },
                    onError = {
                        emitter.onError(it)
                    },
                ).addTo(disposables)
        }.subscribeOn(ioScheduler)
        .observeOn(mainScheduler)
    //endregion
}