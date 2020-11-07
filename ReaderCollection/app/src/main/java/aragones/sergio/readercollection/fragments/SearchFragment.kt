/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.viewmodelfactories.SearchViewModelFactory
import aragones.sergio.readercollection.viewmodels.SearchViewModel

class SearchFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var viewModel: SearchViewModel

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, SearchViewModelFactory(application)).get(SearchViewModel::class.java)
        //TODO use ViewModel
    }
}