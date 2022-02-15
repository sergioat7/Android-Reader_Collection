/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.extensions.getMonthNumber
import aragones.sergio.readercollection.extensions.getPickerParams
import aragones.sergio.readercollection.extensions.getYear
import aragones.sergio.readercollection.extensions.setup
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.ScrollPosition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookListViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Private properties
    private var state: String = ""
    private var sortParam: String? = null
    private var isSortDescending: Boolean = false
    private var query: String = ""
    private var year: Int = -1
    private var month: Int = -1
    private var author: String? = null
    private var format: String? = null
    private val _books = MutableLiveData<List<BookResponse>>(listOf())
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    //endregion

    //region Public properties
    val books: LiveData<List<BookResponse>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun setParams(
        state: String,
        sortParam: String?,
        isSortDescending: Boolean,
        query: String,
        year: Int,
        month: Int,
        author: String?,
        format: String?
    ) {
        this.state = state
        this.sortParam = sortParam
        this.isSortDescending = isSortDescending
        this.query = query
        this.year = year
        this.month = month
        this.author = author
        this.format = format
    }

    fun fetchBooks() {

        _booksLoading.value = true
        booksRepository.getBooksDatabaseObserver(
            format,
            state,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {

                noBooksError()
            },
            onSuccess = {

                if (it.isEmpty()) {
                    noBooksError()
                } else {

                    val sortedBooks = if (isSortDescending) it.reversed() else it
                    var filteredBooks = sortedBooks
                        .filter { book ->
                            book.title?.contains(query, true) ?: false
                        }.filter { book ->
                            book.authorsToString().contains(author ?: "")
                        }
                    if (year >= 0) {
                        filteredBooks = filteredBooks.filter { book ->
                            book.readingDate.getYear() == year
                        }
                    }
                    if (month in 0..11) {
                        filteredBooks = filteredBooks.filter { book ->
                            book.readingDate.getMonthNumber() == month
                        }
                    }
                    _books.value = filteredBooks
                    _booksLoading.value = false
                }
            },
            onError = {

                noBooksError()
            }
        ).addTo(disposables)
    }

    fun setPosition(newPosition: ScrollPosition) {
        _scrollPosition.value = newPosition
    }

    fun sort(context: Context) {

        val sortingKeys = context.resources.getStringArray(R.array.sorting_param_keys)
        val sortingValues = context.resources.getStringArray(R.array.sorting_param_values)

        val dialogView = LinearLayout(context)
        dialogView.orientation = LinearLayout.HORIZONTAL

        val sortKeysPicker = NumberPicker(context)
        sortKeysPicker.setup(sortingValues)
        sortParam?.let {
            sortKeysPicker.value = sortingKeys.indexOf(it)
        }

        val sortOrdersPicker = NumberPicker(context)
        sortOrdersPicker.setup(context.resources.getStringArray(R.array.sorting_order_values))
        sortOrdersPicker.value = if (isSortDescending) 1 else 0

        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        dialogView.layoutParams = params
        dialogView.addView(sortKeysPicker, getPickerParams())
        dialogView.addView(sortOrdersPicker, getPickerParams())

        MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.order_by))
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.accept)) { dialog, _ ->

                val sort = sortingKeys[sortKeysPicker.value]
                sortParam = sort.ifBlank { null }
                isSortDescending = sortOrdersPicker.value == 1
                fetchBooks()
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    //endregion

    //region Private methods
    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
        onDestroy()
    }
    //endregion
}