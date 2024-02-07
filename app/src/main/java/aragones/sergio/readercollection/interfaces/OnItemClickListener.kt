/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.interfaces

interface OnItemClickListener {

    fun onItemClick(bookId: String)
    fun onLoadMoreItemsClick()
    fun onShowAllItemsClick(state: String)
}