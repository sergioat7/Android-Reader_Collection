/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/11/2023
 */

package aragones.sergio.readercollection.presentation.interfaces

import aragones.sergio.readercollection.presentation.ui.books.BooksViewHolder

interface ItemMoveListener {

    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onRowSelected(myViewHolder: BooksViewHolder)
    fun onRowClear(myViewHolder: BooksViewHolder)
}