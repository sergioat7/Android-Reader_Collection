/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository,
    userRepository: UserRepository
) : BaseViewModel() {

    //MARK: - Private properties

    private val _originalBooks = MutableLiveData<List<BookResponse>>()
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private var sortParam = userRepository.sortParam
    private var _sortDescending = MutableLiveData<Boolean?>()

    //MARK: - Public properties

    var query: String = ""
    val books: LiveData<List<BookResponse>> = _books
    val readingBooks: LiveData<List<BookResponse>> =
        _books.map { it.filter { book -> book.state == Constants.READING_STATE } }
    val pendingBooks: LiveData<List<BookResponse>> =
        _books.map { it.filter { book -> book.state == Constants.PENDING_STATE } }
    val readBooks: LiveData<List<BookResponse>> =
        _books.map { it.filter { book -> book.state != Constants.READING_STATE && book.state != Constants.PENDING_STATE } }
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError

    // MARK: - Lifecycle methods

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }

    //MARK: - Public methods

    fun getBooks() {

        _booksLoading.value = true
        booksRepository.getBooksDatabaseObserver(
            null,
            null,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {

                _originalBooks.value = listOf()
                _books.value = listOf()
                _booksLoading.value = false
            },
            onSuccess = {

                _originalBooks.value = if (_sortDescending.value == true) it.reversed() else it
                searchBooks(query)
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = Constants.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun sort(context: Context, sortingKeys: Array<String>, sortingValues: Array<String>) {

        val dialogView = LinearLayout(context)
        dialogView.orientation = LinearLayout.HORIZONTAL

        val sortKeysPicker = Constants.getPicker(context, sortingValues)
        sortParam?.let {
            sortKeysPicker.value = Constants.getValuePositionInArray(it, sortingKeys)
        }

        val values = arrayOf(
            context.resources.getString(R.string.ascending),
            context.resources.getString(R.string.descending)
        )
        val sortOrdersPicker = Constants.getPicker(context, values)
        _sortDescending.value?.let {
            sortOrdersPicker.value = if (it) 1 else 0
        }

        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        dialogView.layoutParams = params
        dialogView.addView(sortKeysPicker, Constants.getPickerParams())
        dialogView.addView(sortOrdersPicker, Constants.getPickerParams())

        AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.order_by))
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.accept)) { dialog, _ ->

                val sort = sortingKeys[sortKeysPicker.value]
                sortParam = if (sort.isNotBlank()) sort else null
                _sortDescending.value = sortOrdersPicker.value == 1
                getBooks()
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun searchBooks(query: String) {

        this.query = query
        _books.value = _originalBooks.value?.filter { book ->
            book.title?.contains(query, true) ?: false
        } ?: listOf()
    }
}