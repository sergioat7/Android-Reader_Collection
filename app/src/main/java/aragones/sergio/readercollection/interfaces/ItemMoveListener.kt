/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/11/2023
 */

package aragones.sergio.readercollection.interfaces

import aragones.sergio.readercollection.ui.books.BooksViewHolder

interface ItemMoveListener {

    fun onRowMoved(fromPosition: Int, toPosition: Int)
    fun onRowSelected(myViewHolder: BooksViewHolder)
    fun onRowClear(myViewHolder: BooksViewHolder)
}