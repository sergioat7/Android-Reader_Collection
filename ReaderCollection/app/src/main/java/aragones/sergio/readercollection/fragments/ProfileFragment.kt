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
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Preferences
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment: BaseFragment() {

    //region Private properties
    private lateinit var etUsername: EditText
    private lateinit var ibInfo: ImageButton
    private lateinit var etPassword: EditText
    private lateinit var ibPassword: ImageButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var rbSpanish: RadioButton
    private lateinit var spSortParams: Spinner
    private lateinit var spSortOrders: Spinner
    private lateinit var spAppTheme: Spinner
    private lateinit var btSave: Button

    private lateinit var viewModel: ProfileViewModel
    //endregion

    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_profile, container, false)
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

        when(item.itemId) {
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
        etUsername = edit_text_username
        ibInfo = image_button_info
        etPassword = edit_text_password
        ibPassword = image_button_password
        rbEnglish = radio_button_en
        rbSpanish = radio_button_es
        spSortParams = spinner_sort_params
        spSortOrders = spinner_sort_orders
        spAppTheme = spinner_app_theme
        btSave = button_save

        viewModel = ViewModelProvider(this, ProfileViewModelFactory(application))[ProfileViewModel::class.java]
        setupBindings()

        etUsername.setText(viewModel.userData.username)
        etUsername.setReadOnly(true, InputType.TYPE_NULL, 0)
        etPassword.setText(viewModel.userData.password)
        rbEnglish.isChecked = viewModel.language == Preferences.ENGLISH_LANGUAGE_KEY
        rbSpanish.isChecked = viewModel.language == Preferences.SPANISH_LANGUAGE_KEY

        etPassword.afterTextChanged {
            viewModel.profileDataChanged(it)
        }

        ibInfo.setOnClickListener {
            showPopupDialog(resources.getString(R.string.username_info))
        }

        ibPassword.setOnClickListener {
            etPassword.showOrHidePassword(ibPassword)
        }

        var position = 0
        viewModel.sortParam?.let { sortParam ->
            position = resources.getStringArray(R.array.sorting_keys_ids).indexOf(sortParam)
        }
        spSortParams.setup(
            resources.getStringArray(R.array.sorting_keys).toList(),
            position,
            true
        )

        spSortOrders.setup(
            listOf(resources.getString(R.string.ascending), resources.getString(R.string.descending)),
            if(viewModel.isSortDescending) 1 else 0,
            true
        )

        spAppTheme.setup(
            resources.getStringArray(R.array.app_theme_values).toList(),
            viewModel.themeMode,
            true
        )

        btSave.setOnClickListener {

            val language =
                if (rbEnglish.isChecked) Preferences.ENGLISH_LANGUAGE_KEY
                else Preferences.SPANISH_LANGUAGE_KEY
            val sortParam =
                if (spSortParams.selectedItemPosition == 0) null
                else resources.getStringArray(R.array.sorting_keys_ids)[spSortParams.selectedItemPosition]
            val isSortDescending = spSortOrders.selectedItemPosition == 1
            val themeMode = spAppTheme.selectedItemPosition
            viewModel.save(
                etPassword.text.toString(),
                language,
                sortParam,
                isSortDescending,
                themeMode
            )
        }
    }

    private fun setupBindings() {

        viewModel.profileForm.observe(viewLifecycleOwner, Observer {

            etPassword.clearErrors()
            btSave.isEnabled = it == null

            val passwordError = it ?: return@Observer
            etPassword.error = getString(passwordError)
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