/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2022
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentStatisticsBinding
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.StatisticsViewModelFactory
import aragones.sergio.readercollection.viewmodels.StatisticsViewModel

class StatisticsFragment : BindingFragment<FragmentStatisticsBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: StatisticsViewModel
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(application)
        )[StatisticsViewModel::class.java]
    }
    //endregion
}