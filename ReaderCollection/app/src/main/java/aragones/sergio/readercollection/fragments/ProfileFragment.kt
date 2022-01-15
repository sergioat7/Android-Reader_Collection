/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentProfileBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.utils.Preferences
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BindingFragment<FragmentProfileBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: ProfileViewModel
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
        inflater.inflate(R.menu.profile_toolbar_menu, menu)
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
            this@ProfileFragment.viewModel.save(
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
            ProfileViewModelFactory(application)
        )[ProfileViewModel::class.java]
        setupBindings()

        binding.textInputLayoutUsername.setEndIconOnClickListener {
            showPopupDialog(resources.getString(R.string.username_info))
        }

        val sortParams = resources.getStringArray(R.array.sorting_keys).toList()
        binding.dropdownTextInputLayoutSortParams.setup(sortParams)
        viewModel.sortParam?.let { sortParam ->
            val currentSortParam = sortParams[resources.getStringArray(R.array.sorting_keys_ids).indexOf(sortParam)]
            binding.dropdownTextInputLayoutSortParams.setValue(currentSortParam)
        } ?: run {
            binding.dropdownTextInputLayoutSortParams.setValue(sortParams[0])
        }

        val sortOrders = listOf(
            resources.getString(R.string.ascending),
            resources.getString(R.string.descending)
        )
        binding.dropdownTextInputLayoutSortOrders.setup(sortOrders)
        val currentSortOrder = if (viewModel.isSortDescending) sortOrders[1] else sortOrders[0]
        binding.dropdownTextInputLayoutSortOrders.setValue(currentSortOrder)

        val appThemes = resources.getStringArray(R.array.app_theme_values).toList()
        binding.dropdownTextInputLayoutAppTheme.setup(appThemes)
        binding.dropdownTextInputLayoutAppTheme.setValue(appThemes[viewModel.themeMode])

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

        viewModel.profileLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.profileError.observe(viewLifecycleOwner, { error ->
            manageError(error)
        })
    }
    //endregion
}