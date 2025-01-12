/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.presentation.ui.base

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.extensions.getPickerParams
import aragones.sergio.readercollection.presentation.extensions.setup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {

    val disposables = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }

    fun sort(
        context: Context,
        sortParam: String?,
        isSortDescending: Boolean,
        acceptHandler: ((newSortParam: String?, newIsSortDescending: Boolean) -> Unit)?,
    ) {
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

                val sortValue = sortingKeys[sortKeysPicker.value]
                acceptHandler?.invoke(
                    sortValue.ifBlank { null },
                    sortOrdersPicker.value == 1,
                )
                dialog.dismiss()
            }.setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}