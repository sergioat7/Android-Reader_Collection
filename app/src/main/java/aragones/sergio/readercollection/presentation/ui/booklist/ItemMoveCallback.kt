/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/11/2023
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import aragones.sergio.readercollection.presentation.interfaces.ItemMoveListener
import aragones.sergio.readercollection.presentation.ui.books.BooksViewHolder

class ItemMoveCallback(
    private val listener: ItemMoveListener
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        val flags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(flags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        listener.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is BooksViewHolder) {
                listener.onRowSelected(viewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        if (viewHolder is BooksViewHolder) {
            listener.onRowClear(viewHolder)
        }
    }
}