/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package com.aragones.sergio.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.aragones.sergio.data.business.BookResponse
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

    @Query("SELECT * FROM Book WHERE state == 'PENDING'")
    fun getPendingBooksObserver(): Maybe<List<BookResponse>>

    @Query("SELECT * FROM Book WHERE id == :id")
    fun getBookObserver(id: String): Single<BookResponse>
}