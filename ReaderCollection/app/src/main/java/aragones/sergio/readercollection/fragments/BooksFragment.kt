/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.viewmodels.BooksViewModel
import kotlinx.android.synthetic.main.fragment_books.*

class BooksFragment : Fragment() {

    private lateinit var booksViewModel: BooksViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        booksViewModel = ViewModelProvider(this).get(BooksViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_books, container, false)
        booksViewModel.text.observe(viewLifecycleOwner, Observer {
            text_books.text = it
        })
        return root
    }
}