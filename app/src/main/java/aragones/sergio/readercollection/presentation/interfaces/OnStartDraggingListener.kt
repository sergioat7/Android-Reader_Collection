/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 26/11/2023
 */

package aragones.sergio.readercollection.presentation.interfaces

import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.books.BooksViewHolder

interface OnStartDraggingListener {

    fun onStartDragging(viewHolder: BooksViewHolder)
    fun onFinishDragging(books: List<Book>)
}