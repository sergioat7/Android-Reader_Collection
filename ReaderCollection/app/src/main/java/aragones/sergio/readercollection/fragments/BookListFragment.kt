/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/1/2022
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentBookListBinding
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.utils.StatusBarStyle

class BookListFragment : BindingFragment<FragmentBookListBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val args: BookListFragmentArgs by navArgs()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun initializeUi() {
        super.initializeUi()

        binding.isDarkMode = context?.isDarkMode()
    }
    //endregion
}