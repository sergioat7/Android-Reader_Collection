/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodels.ProfileViewModel

class ProfileFragment: Fragment() {

    //MARK: - Private properties

    private lateinit var viewModel: ProfileViewModel

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(this, ProfileViewModelFactory(application)).get(ProfileViewModel::class.java)
        //TODO use the ViewModel
    }
}