/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/11/2020
 */

package aragones.sergio.readercollection.adapters

interface OnItemClickListener {

    fun onItemClick(bookId: String)
    fun onLoadMoreItemsClick()
}