/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.text.InputType
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
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : BindingFragment<FragmentProfileBinding>() {

    //region Private properties
    private lateinit var viewModel: ProfileViewModel
    //endregion

    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
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

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(application)
        )[ProfileViewModel::class.java]
        setupBindings()

        with(binding) {

            editTextUsername.setText(viewModel.userData.username)
            editTextUsername.setReadOnly(true, InputType.TYPE_NULL, 0)
            editTextPassword.setText(viewModel.userData.password)
            radioButtonEn.isChecked = viewModel.language == Preferences.ENGLISH_LANGUAGE_KEY
            radioButtonEs.isChecked = viewModel.language == Preferences.SPANISH_LANGUAGE_KEY

            editTextPassword.afterTextChanged {
                viewModel.profileDataChanged(it)
            }

            imageButtonInfo.setOnClickListener {
                showPopupDialog(resources.getString(R.string.username_info))
            }

            imageButtonPassword.setOnClickListener {
                editTextPassword.showOrHidePassword(imageButtonPassword)
            }

            var position = 0
            viewModel.sortParam?.let { sortParam ->
                position = resources.getStringArray(R.array.sorting_keys_ids).indexOf(sortParam)
            }
            spinnerSortParams.setup(
                resources.getStringArray(R.array.sorting_keys).toList(),
                position,
                true
            )

            spinnerSortOrders.setup(
                listOf(
                    resources.getString(R.string.ascending),
                    resources.getString(R.string.descending)
                ),
                if (viewModel.isSortDescending) 1 else 0,
                true
            )

            spinnerAppTheme.setup(
                resources.getStringArray(R.array.app_theme_values).toList(),
                viewModel.themeMode,
                true
            )

            buttonSave.setOnClickListener {

                val language =
                    if (radioButtonEn.isChecked) Preferences.ENGLISH_LANGUAGE_KEY
                    else Preferences.SPANISH_LANGUAGE_KEY
                val sortParam =
                    if (spinnerSortParams.selectedItemPosition == 0) null
                    else resources.getStringArray(R.array.sorting_keys_ids)[spinnerSortParams.selectedItemPosition]
                val isSortDescending = spinnerSortOrders.selectedItemPosition == 1
                val themeMode = spinnerAppTheme.selectedItemPosition
                viewModel.save(
                    editTextPassword.text.toString(),
                    language,
                    sortParam,
                    isSortDescending,
                    themeMode
                )
            }
        }
    }

    private fun setupBindings() {

        viewModel.profileForm.observe(viewLifecycleOwner, Observer {

            binding.editTextPassword.clearErrors()
            binding.buttonSave.isEnabled = it == null

            val passwordError = it ?: return@Observer
            binding.editTextPassword.error = getString(passwordError)
        })

        viewModel.profileRedirection.observe(viewLifecycleOwner, Observer { redirect ->

            if (!redirect) return@Observer
            launchActivity(LandingActivity::class.java)
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