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
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBooks(books: List<Book>)

    @Delete
    suspend fun deleteBooks(books: List<Book>)

    @Query("SELECT * FROM book")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE state == 'READ'")
    fun getReadBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE id == :id")
    suspend fun getBook(id: String): Book?
}