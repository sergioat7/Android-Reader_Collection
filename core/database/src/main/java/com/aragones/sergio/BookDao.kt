/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package com.aragones.sergio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.aragones.sergio.model.Book
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBooks(books: List<Book>): Completable

    @Delete
    fun deleteBooks(books: List<Book>): Completable

    @RawQuery
    fun getBooks(query: SupportSQLiteQuery): Maybe<List<Book>>

    @Query("SELECT * FROM Book WHERE state == 'PENDING'")
    fun getPendingBooks(): Maybe<List<Book>>

    @Query("SELECT * FROM Book WHERE id == :id")
    fun getBook(id: String): Single<Book>
}