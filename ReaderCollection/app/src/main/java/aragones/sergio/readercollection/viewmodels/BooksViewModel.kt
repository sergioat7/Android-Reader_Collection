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
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository
) : BaseViewModel() {

    //MARK: - Private properties

    private val _originalBooks = MutableLiveData<List<BookResponse>>()
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _formats = MutableLiveData<List<FormatResponse>>()
    private val _states = MutableLiveData<List<StateResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksFormatsLoading = MutableLiveData<Boolean>()
    private val _booksStatesLoading = MutableLiveData<Boolean>()
    private val _bookSet = MutableLiveData<Int?>()
    private val _bookDeleted = MutableLiveData<Int?>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private var _selectedFormat = MutableLiveData<String?>()
    private var _selectedState = MutableLiveData<String?>()
    private var _isFavourite = MutableLiveData<Boolean?>()
    private var _sortKey = MutableLiveData<String?>()
    private var _sortDescending = MutableLiveData<Boolean?>()

    //MARK: - Public properties

    val books: LiveData<List<BookResponse>> = _books
    val formats: LiveData<List<FormatResponse>> = _formats
    val states: LiveData<List<StateResponse>> = _states
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksFormatsLoading: LiveData<Boolean> = _booksFormatsLoading
    val booksStatesLoading: LiveData<Boolean> = _booksStatesLoading
    val bookSet: LiveData<Int?> = _bookSet
    val bookDeleted: LiveData<Int?> = _bookDeleted
    val booksError: LiveData<ErrorResponse> = _booksError
    val selectedFormat: LiveData<String?> = _selectedFormat
    val selectedState: LiveData<String?> = _selectedState
    val isFavourite: LiveData<Boolean?> = _isFavourite
    val isRefreshEnabled: Boolean
        get() = sharedPreferencesHandler.getSwipeRefresh()

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
            _selectedFormat.value,
            _selectedState.value,
            _isFavourite.value,
            _sortKey.value
        ).subscribeBy(
            onComplete = {

                _originalBooks.value = listOf()
                _books.value = listOf()
                _booksLoading.value = false
            },
            onSuccess = {

                _originalBooks.value = if (_sortDescending.value == true) it.reversed() else it
                _books.value = if (_sortDescending.value == true) it.reversed() else it
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = Constants.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun getFormats() {

        _booksFormatsLoading.value = true
        formatRepository.getFormatsDatabaseObserver().subscribeBy(
            onSuccess = {

                _formats.value = it
                _booksFormatsLoading.value = false
            },
            onError = {

                _formats.value = listOf()
                _booksFormatsLoading.value = false
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun getStates() {

        _booksStatesLoading.value = true
        stateRepository.getStatesDatabaseObserver().subscribeBy(
            onSuccess = {

                _states.value = it
                _booksStatesLoading.value = false
            },
            onError = {

                _states.value = listOf()
                _booksStatesLoading.value = false
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun getSortParam() {
        _sortKey.value = booksRepository.sortParam
    }

    fun reloadData() {
        _books.value = mutableListOf()
    }

    fun setFormat(format: String?) {
        _selectedFormat.value = format
    }

    fun setState(state: String?) {
        _selectedState.value = state
    }

    fun setFavourite(isFavourite: Boolean?) {
        _isFavourite.value = isFavourite
    }

    fun sort(context: Context, sortingKeys: Array<String>, sortingValues: Array<String>) {

        val dialogView = LinearLayout(context)
        dialogView.orientation = LinearLayout.HORIZONTAL

        val sortKeysPicker = Constants.getPicker(context, sortingValues)
        _sortKey.value?.let {
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
                _sortKey.value = if (sort.isNotBlank()) sort else null
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

        _books.value = _originalBooks.value?.filter { book ->
            book.title?.contains(query, true) ?: false
        } ?: listOf()
    }

    fun setBookFavourite(position: Int) {
        _books.value?.get(position)?.let { book ->

            _booksLoading.value = true
            booksRepository.setFavouriteBookObserver(book.id, !book.isFavourite).subscribeBy(
                onSuccess = {

                    _booksLoading.value = false
                    _books.value?.first { it.id == book.id }?.isFavourite = !book.isFavourite
                    _bookSet.value = position
                    _bookSet.value = null
                },
                onError = {

                    _booksLoading.value = false
                    _bookSet.value = null
                    onDestroy()
                }
            ).addTo(disposables)
        }
    }

    fun deleteBook(position: Int) {
        _books.value?.get(position)?.let { book ->

            _booksLoading.value = true
            booksRepository.deleteBookObserver(book.id).subscribeBy(
                onComplete = {

                    _booksLoading.value = false
                    _bookDeleted.value = position
                    _bookDeleted.value = null
                },
                onError = {

                    _booksLoading.value = false
                    _bookDeleted.value = null
                    onDestroy()
                }
            ).addTo(disposables)

        }
    }
}