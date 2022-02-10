/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentSettingsBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.utils.CustomDropdownType
import aragones.sergio.readercollection.utils.Preferences
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.SettingsViewModelFactory
import aragones.sergio.readercollection.viewmodels.SettingsViewModel

class SettingsFragment : BindingFragment<FragmentSettingsBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: SettingsViewModel
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.settings_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_delete -> {

                showPopupConfirmationDialog(R.string.profile_delete_confirmation, acceptHandler = {
                    viewModel.deleteUser()
                })
                return true
            }
            R.id.action_logout -> {

                showPopupConfirmationDialog(R.string.profile_logout_confirmation, acceptHandler = {
                    viewModel.logout()
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        binding.textInputLayoutPassword.doAfterTextChanged {
            viewModel.profileDataChanged(it.toString())
        }
        setupDropdowns()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Public methods
    fun save() {
        with(binding) {

            val language =
                if (radioButtonEn.isChecked) Preferences.ENGLISH_LANGUAGE_KEY
                else Preferences.SPANISH_LANGUAGE_KEY
            val sortParam =
                if (dropdownTextInputLayoutSortParams.getPosition() == 0) null
                else resources.getStringArray(R.array.sorting_param_keys)[dropdownTextInputLayoutSortParams.getPosition()]
            val isSortDescending = dropdownTextInputLayoutSortOrders.getPosition() == 1
            val themeMode = dropdownTextInputLayoutAppTheme.getPosition()
            this@SettingsFragment.viewModel.save(
                textInputLayoutPassword.getValue(),
                language,
                sortParam,
                isSortDescending,
                themeMode
            )
        }
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(application)
        )[SettingsViewModel::class.java]
        setupBindings()

        binding.textInputLayoutUsername.setEndIconOnClickListener {
            showPopupDialog(resources.getString(R.string.username_info))
        }

        binding.fragment = this
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.profileForm.observe(viewLifecycleOwner, Observer {

            binding.textInputLayoutPassword.setError("")

            val passwordError = it ?: return@Observer
            binding.textInputLayoutPassword.setError(getString(passwordError))
        })

        viewModel.profileRedirection.observe(viewLifecycleOwner, Observer { redirect ->

            if (!redirect) return@Observer
            launchActivity(LandingActivity::class.java, true)
            activity?.finish()
        })

        viewModel.profileLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.profileError.observe(viewLifecycleOwner) { error ->
            manageError(error)
        }
    }

    private fun setupDropdowns() {

        val sortParamKeys = resources.getStringArray(R.array.sorting_param_keys).toList()
        binding.dropdownTextInputLayoutSortParams.setValue(
            viewModel.sortParam ?: sortParamKeys[0],
            CustomDropdownType.SORT_PARAM
        )

        val sortOrderKeys = resources.getStringArray(R.array.sorting_order_keys).toList()
        binding.dropdownTextInputLayoutSortOrders.setValue(
            if (viewModel.isSortDescending) sortOrderKeys[1] else sortOrderKeys[0],
            CustomDropdownType.SORT_ORDER
        )

        val appThemes = resources.getStringArray(R.array.app_theme_values).toList()
        binding.dropdownTextInputLayoutAppTheme.setValue(
            appThemes[viewModel.themeMode],
            CustomDropdownType.APP_THEME
        )
    }
    //endregion
}