/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 26/11/2023
 */

package aragones.sergio.readercollection.interfaces

import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.ui.books.BooksViewHolder

interface OnStartDraggingListener {

    fun onStartDragging(viewHolder: BooksViewHolder)
    fun onFinishDragging(books: List<Book>)
}