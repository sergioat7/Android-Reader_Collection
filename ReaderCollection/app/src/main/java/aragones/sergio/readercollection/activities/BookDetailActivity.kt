/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2020
 */

package aragones.sergio.readercollection.activities

import android.os.Bundle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseActivity
import aragones.sergio.readercollection.fragments.BookDetailFragment
import aragones.sergio.readercollection.utils.Constants
import kotlinx.android.synthetic.main.activity_book_detail.*

class BookDetailActivity: BaseActivity() {

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bookId = intent.getStringExtra(Constants.BOOK_ID)
        val isGoogleBook = intent.getBooleanExtra(Constants.IS_GOOGLE_BOOK, false)
        val bookDetailFragment = BookDetailFragment.newInstance()

        val bundle = Bundle()
        bundle.putString(Constants.BOOK_ID, bookId)
        bundle.putBoolean(Constants.IS_GOOGLE_BOOK, isGoogleBook)
        bookDetailFragment.arguments = bundle

        if (savedInstanceState == null) {

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, bookDetailFragment)
                .commitNow()
        }
    }
    //endregion
}