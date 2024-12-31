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
import androidx.room.Update
import com.aragones.sergio.model.Book
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBooks(books: List<Book>): Completable

    @Delete
    fun deleteBooks(books: List<Book>): Completable

    @Query("SELECT * FROM Book")
    fun getAllBooks(): Flowable<List<Book>>

    @Query("SELECT * FROM Book WHERE state == 'READ'")
    fun getReadBooks(): Flowable<List<Book>>

    @Query("SELECT * FROM Book WHERE id == :id")
    fun getBook(id: String): Single<Book>
}