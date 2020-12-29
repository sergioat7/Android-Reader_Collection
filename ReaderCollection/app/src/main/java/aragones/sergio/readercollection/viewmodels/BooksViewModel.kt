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
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.models.responses.FormatResponse
import aragones.sergio.readercollection.models.responses.StateResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): ViewModel() {

    //MARK: - Private properties

    private val _books = MutableLiveData<List<BookResponse>>()
    private val _formats = MutableLiveData<List<FormatResponse>>()
    private val _states = MutableLiveData<List<StateResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksFormatsLoading = MutableLiveData<Boolean>()
    private val _booksStatesLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private var _selectedFormat = MutableLiveData<String?>()
    private var _selectedState = MutableLiveData<String?>()
    private var _isFavourite = MutableLiveData<Boolean?>()
    private var _sortKey = MutableLiveData<String?>()

    //MARK: - Public properties

    val books: LiveData<List<BookResponse>> = _books
    val formats: LiveData<List<FormatResponse>> = _formats
    val states: LiveData<List<StateResponse>> = _states
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksFormatsLoading: LiveData<Boolean> = _booksFormatsLoading
    val booksStatesLoading: LiveData<Boolean> = _booksStatesLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val selectedFormat: LiveData<String?> = _selectedFormat
    val selectedState: LiveData<String?> = _selectedState
    val isFavourite: LiveData<Boolean?> = _isFavourite

    //MARK: - Public methods

    fun getBooks() {

        _booksLoading.value = true
        booksRepository.getBooks(_selectedFormat.value, _selectedState.value, _isFavourite.value).subscribeBy(
            onComplete = {

                _books.value = listOf()
                _booksLoading.value = false
            },
            onSuccess = {

                _books.value = it
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = Constants.handleError(it)
            }
        )
    }

    fun getFormats() {

        _booksFormatsLoading.value = true
        booksRepository.getFormats().subscribeBy(
            onSuccess = {

                _formats.value = it
                _booksFormatsLoading.value = false
            },
            onError = {

                _formats.value = listOf()
                _booksFormatsLoading.value = false
            }
        )
    }

    fun getStates() {

        _booksStatesLoading.value = true
        booksRepository.getStates().subscribeBy(
            onSuccess = {

                _states.value = it
                _booksStatesLoading.value = false
            },
            onError = {

                _states.value = listOf()
                _booksStatesLoading.value = false
            }
        )
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

        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        dialogView.layoutParams = params
        dialogView.addView(sortKeysPicker, Constants.getPickerParams())

        AlertDialog.Builder(context)
            .setTitle(context.resources.getString(R.string.order_by))
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.accept)) { dialog, _ ->

                val sort = sortingKeys[sortKeysPicker.value]
                _sortKey.value = if (sort.isNotBlank()) sort else null
                getBooks()
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}