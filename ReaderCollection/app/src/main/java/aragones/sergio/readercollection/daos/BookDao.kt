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

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<BookResponse>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBooks(books: List<BookResponse>): Completable

    @Delete
    fun deleteBooks(books: List<BookResponse>): Completable

    @RawQuery
    fun getBooks(query: SupportSQLiteQuery): Maybe<List<BookResponse>>
}