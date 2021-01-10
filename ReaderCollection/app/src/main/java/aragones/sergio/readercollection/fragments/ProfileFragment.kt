/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LandingActivity
import aragones.sergio.readercollection.extensions.afterTextChanged
import aragones.sergio.readercollection.extensions.clearErrors
import aragones.sergio.readercollection.extensions.setReadOnly
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.ProfileViewModelFactory
import aragones.sergio.readercollection.viewmodels.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var ibPassword: ImageButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var rbSpanish: RadioButton
    private lateinit var spSortParams: Spinner
    private lateinit var btSave: Button
    private lateinit var viewModel: ProfileViewModel

    //MARK: - Lifecycle methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.clear()
        inflater.inflate(R.menu.profile_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_synchronize -> {

                openSyncPopup()
                return true
            }
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
        viewModel.onDestroy()
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        etUsername = edit_text_username
        etPassword = edit_text_password
        ibPassword = image_button_password
        rbEnglish = radio_button_en
        rbSpanish = radio_button_es
        spSortParams = spinner_sort_params
        btSave = button_save
        viewModel = ViewModelProvider(this, ProfileViewModelFactory(application)).get(ProfileViewModel::class.java)
        setupBindings()

        etUsername.setText(viewModel.userData.username)
        etUsername.setReadOnly(true, InputType.TYPE_NULL, 0)
        etPassword.setText(viewModel.userData.password)
        rbEnglish.isChecked = viewModel.language == Constants.ENGLISH_LANGUAGE_KEY
        rbSpanish.isChecked = viewModel.language == Constants.SPANISH_LANGUAGE_KEY

        etPassword.afterTextChanged {
            viewModel.profileDataChanged(it)
        }

        ibPassword.setOnClickListener {
            Constants.showOrHidePassword(etPassword, ibPassword, Constants.isDarkMode(context))
        }

        spSortParams.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireActivity(),
                R.color.colorPrimary)
        )
        spSortParams.adapter = Constants.getAdapter(
            context = requireContext(),
            data = resources.getStringArray(R.array.sorting_keys).toList(),
            firstOptionEnabled = true
        )
        var position = 0
        viewModel.sortParam?.let { sortParam ->
            position = resources.getStringArray(R.array.sorting_keys_ids).indexOf(sortParam)
        }
        spSortParams.setSelection(position)

        btSave.setOnClickListener {

            val language =
                if (rbEnglish.isChecked) Constants.ENGLISH_LANGUAGE_KEY
                else Constants.SPANISH_LANGUAGE_KEY
            val sortParam =
                if (spSortParams.selectedItemPosition == 0) null
                else resources.getStringArray(R.array.sorting_keys_ids)[spSortParams.selectedItemPosition]
            viewModel.saveData(
                etPassword.text.toString(),
                language,
                sortParam
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
}