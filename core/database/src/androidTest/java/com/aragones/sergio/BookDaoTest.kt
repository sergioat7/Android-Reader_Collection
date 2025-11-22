/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/9/2025
 */

package com.aragones.sergio

import android.database.Cursor
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.aragones.sergio.converters.DateConverter
import com.aragones.sergio.converters.ListConverter
import com.aragones.sergio.model.Book
import javax.inject.Named
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class BookDaoTest {

    private lateinit var database: ReaderCollectionDatabase
    private lateinit var databaseHelper: SupportSQLiteOpenHelper
    private lateinit var bookDao: BookDao
    private val dateConverter = DateConverter()
    private val listConverter = ListConverter()

    private val book = Book(
        id = "id",
        title = "title",
        subtitle = "subtitle",
        authors = listOf(),
        publisher = "publisher",
        publishedDate = null,
        readingDate = null,
        description = "description",
        summary = "summary",
        isbn = "isbn",
        pageCount = 100,
        categories = listOf(),
        averageRating = 5.0,
        ratingsCount = 10,
        rating = 6.5,
        thumbnail = "thumbnail",
        image = "image",
        format = "PHYSICAL",
        state = "PENDING",
        priority = -1,
    )

    @Before
    fun setup() {
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                ReaderCollectionDatabase::class.java,
            ).allowMainThreadQueries()
            .build()
        databaseHelper = database.openHelper

        bookDao = database.bookDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Named(
        """
        GIVEN one book
        WHEN insertBooks is invoked
        THEN one book is inserted
    """,
    )
    @Test
    fun testInsertOneBook() = runTest {
        bookDao.insertBooks(listOf(book))

        val fetchedBook = getFromDatabase(listOf(book.id))?.firstOrNull()
        Assert.assertEquals(book, fetchedBook)
    }

    @Named(
        """
        GIVEN multiple books
        WHEN insertBooks is invoked
        THEN multiple books are inserted
    """,
    )
    @Test
    fun testInsertMultipleBooks() = runTest {
        val book1 = book.copy(id = "1")
        val book2 = book.copy(id = "2")

        bookDao.insertBooks(listOf(book1, book2))

        val books = getFromDatabase(listOf(book1.id, book2.id))
        Assert.assertEquals(listOf(book1, book2), books)
    }

    @Named(
        """
        GIVEN book saved
        WHEN updateBooks is invoked
        THEN book is updated
    """,
    )
    @Test
    fun testUpdateExistentBook() = runTest {
        insertInDatabase(listOf(book))

        val updatedBook = book.copy(title = "updated_title")
        bookDao.updateBooks(listOf(updatedBook))

        val fetchedBook = getFromDatabase(listOf(updatedBook.id))?.firstOrNull()
        Assert.assertEquals(updatedBook, fetchedBook)
    }

    @Named(
        """
        GIVEN no book saved
        WHEN updateBooks is invoked
        THEN nothing
    """,
    )
    @Test
    fun testUpdateNonExistentBook() = runTest {
        val updatedBook = book.copy(title = "updated_title")
        bookDao.updateBooks(listOf(updatedBook))

        val fetchedBook = getFromDatabase(listOf(updatedBook.id))?.firstOrNull()
        Assert.assertEquals(null, fetchedBook)
    }

    @Named(
        """
        GIVEN book saved
        WHEN deleteBooks is invoked
        THEN book is deleted
    """,
    )
    @Test
    fun testDeleteExistentBook() = runTest {
        insertInDatabase(listOf(book))

        bookDao.deleteBooks(listOf(book))

        val fetchedBook = getFromDatabase(listOf(book.id))?.firstOrNull()
        Assert.assertEquals(null, fetchedBook)
    }

    @Named(
        """
        GIVEN book saved
        WHEN deleteBooks is invoked
        THEN nothing
    """,
    )
    @Test
    fun testDeleteNonExistentBook() = runTest {
        bookDao.deleteBooks(listOf(book))

        val fetchedBook = getFromDatabase(listOf(book.id))?.firstOrNull()
        Assert.assertEquals(null, fetchedBook)
    }

    @Named(
        """
        GIVEN multiple books saved
        WHEN getAll is invoked
        THEN return multiple books
    """,
    )
    @Test
    fun testGetMultipleBooks() = runTest {
        val book1 = book.copy(id = "1")
        val book2 = book.copy(id = "2")
        insertInDatabase(listOf(book1, book2))

        val books = bookDao.getAllBooks().first()

        Assert.assertEquals(listOf(book1, book2), books)
    }

    @Named(
        """
        GIVEN no books saved
        WHEN getAll is invoked
        THEN return none
    """,
    )
    @Test
    fun testGetNoBooks() = runTest {
        val books = bookDao.getAllBooks().first()

        Assert.assertEquals(listOf<Book>(), books)
    }

    @Named(
        """
        GIVEN multiple read books saved
        WHEN getReadBooks is invoked
        THEN return multiple read books
    """,
    )
    @Test
    fun testGetReadBooks() = runTest {
        val book1 = book.copy(id = "1", state = "READ")
        val book2 = book.copy(id = "2")
        val book3 = book.copy(id = "3", state = "READ")
        val book4 = book.copy(id = "4")
        insertInDatabase(listOf(book1, book2, book3, book4))

        val books = bookDao.getReadBooks().first().sortedBy { it.id }

        Assert.assertEquals(listOf(book1, book3), books)
    }

    @Named(
        """
        GIVEN multiple books saved but none read
        WHEN getReadBooks is invoked
        THEN return none
    """,
    )
    @Test
    fun testGetNoReadBooks() = runTest {
        val book1 = book.copy(id = "1")
        val book2 = book.copy(id = "2")
        insertInDatabase(listOf(book1, book2))

        val books = bookDao.getReadBooks().first()

        Assert.assertEquals(listOf<Book>(), books)
    }

    @Named(
        """
        GIVEN existent book saved
        WHEN getBook is invoked
        THEN return book
    """,
    )
    @Test
    fun testGetBook() = runTest {
        insertInDatabase(listOf(book))

        val fetchedBook = bookDao.getBook(book.id)

        Assert.assertEquals(book, fetchedBook)
    }

    @Named(
        """
        GIVEN no book saved
        WHEN getBook is invoked
        THEN return none
    """,
    )
    @Test
    fun testGetNoBook() = runTest {
        insertInDatabase(listOf(book))

        val fetchedBook = bookDao.getBook("another_id")

        Assert.assertEquals(null, fetchedBook)
    }

    private fun insertInDatabase(books: List<Book>) {
        books.forEach {
            databaseHelper.writableDatabase.execSQL(
                """
                INSERT INTO Book (id, title, subtitle, authors, publisher, publishedDate, readingDate, description, summary, isbn, pageCount, categories, averageRating, ratingsCount, rating, thumbnail, image, format, state, priority) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                it.toSql(),
            )
        }
    }

    private fun getFromDatabase(ids: List<String>): List<Book>? {
        val placeholders = ids.joinToString(separator = ",") { "?" }
        val cursor = databaseHelper.readableDatabase.query(
            "SELECT * FROM book WHERE id IN ($placeholders)",
            ids.toTypedArray(),
        )
        val books = mutableListOf<Book>()
        cursor.use {
            while (it.moveToNext()) {
                books.add(fromSql(it))
            }
        }
        return books.ifEmpty { null }
    }

    private fun fromSql(cursor: Cursor): Book = Book(
        id = cursor.getString(0),
        title = cursor.getString(1),
        subtitle = cursor.getString(2),
        authors = listConverter.stringToStringList(cursor.getStringOrNull(3)),
        publisher = cursor.getString(4),
        publishedDate = dateConverter.toDate(cursor.getLongOrNull(5)),
        readingDate = dateConverter.toDate(cursor.getLongOrNull(6)),
        description = cursor.getString(7),
        summary = cursor.getString(8),
        isbn = cursor.getString(9),
        pageCount = cursor.getInt(10),
        categories = listConverter.stringToStringList(cursor.getStringOrNull(11)),
        averageRating = cursor.getDouble(12),
        ratingsCount = cursor.getInt(13),
        rating = cursor.getDouble(14),
        thumbnail = cursor.getString(15),
        image = cursor.getString(16),
        format = cursor.getString(17),
        state = cursor.getString(18),
        priority = cursor.getInt(19),
    )

    private fun Book.toSql(): Array<out Any?> = arrayOf(
        id,
        title,
        subtitle,
        listConverter.stringListToString(authors),
        publisher,
        dateConverter.fromDate(publishedDate),
        dateConverter.fromDate(readingDate),
        description,
        summary,
        isbn,
        pageCount,
        listConverter.stringListToString(categories),
        averageRating,
        ratingsCount,
        rating,
        thumbnail,
        image,
        format,
        state,
        priority,
    )
}