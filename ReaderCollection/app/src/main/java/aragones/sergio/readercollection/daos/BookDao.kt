/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/1/2021
 */

package aragones.sergio.readercollection.daos

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import aragones.sergio.readercollection.models.responses.BookResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooksObserver(books: List<BookResponse>): Completable

    @RawQuery
    fun insertBooksObserver(query: SupportSQLiteQuery): Single<List<BookResponse>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBooksObserver(books: List<BookResponse>): Completable

    @Delete
    fun deleteBooksObserver(books: List<BookResponse>): Completable

    @RawQuery
    fun getBooksObserver(query: SupportSQLiteQuery): Maybe<List<BookResponse>>

    @Query("SELECT * FROM Book WHERE id == :bookId")
    fun getBookObserver(bookId: String): Single<BookResponse>
}