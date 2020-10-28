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
import aragones.sergio.readercollection.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment: Fragment() {

    //MARK: - Private properties

    private lateinit var searchViewModel: SearchViewModel

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        searchViewModel.text.observe(viewLifecycleOwner, Observer {
            text_search.text = it
        })
        return root
    }
}