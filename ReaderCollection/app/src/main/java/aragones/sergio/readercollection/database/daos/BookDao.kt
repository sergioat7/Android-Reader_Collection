/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.database.daos

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import aragones.sergio.readercollection.models.BookResponse
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooksObserver(books: List<BookResponse>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBooksObserver(books: List<BookResponse>): Completable

    @Delete
    fun deleteBooksObserver(books: List<BookResponse>): Completable

    @RawQuery
    fun getBooksObserver(query: SupportSQLiteQuery): Maybe<List<BookResponse>>

    @Query("SELECT * FROM Book WHERE id == :bookId")
    fun getBookObserver(bookId: String): Single<BookResponse>
}