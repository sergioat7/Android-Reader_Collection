/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.LoginActivity
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
            R.id.action_delete -> {

                return true
            }
            R.id.action_logout -> {

                viewModel.logout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        etUsername = edit_text_username
        etPassword = edit_text_password
        ibPassword = image_button_password
        rbEnglish = radio_button_en
        rbSpanish = radio_button_es
        btSave = button_save
        viewModel = ViewModelProvider(this, ProfileViewModelFactory(application)).get(ProfileViewModel::class.java)
        setupBindings()

        etUsername.setText(viewModel.profileUserData.value?.username)
        etPassword.setText(viewModel.profileUserData.value?.password)
        etUsername.setReadOnly(true, InputType.TYPE_NULL, 0)

        etPassword.afterTextChanged {
            viewModel.profileDataChanged(it)
        }

        ibPassword.setOnClickListener {
            Constants.showOrHidePassword(etPassword, ibPassword, Constants.isDarkMode(context))
        }

    }

    private fun setupBindings() {

        viewModel.profileUserData.observe(viewLifecycleOwner, Observer {
            viewModel.login(it.username, it.password)
        })

        viewModel.profileForm.observe(viewLifecycleOwner, Observer {

            etPassword.clearErrors()
            btSave.isEnabled = it == null

            val passwordError = it ?: return@Observer
            etPassword.error = getString(passwordError)
        })

        viewModel.profileRedirection.observe(viewLifecycleOwner, Observer { redirect ->

            if (!redirect) return@Observer
            launchActivity(LoginActivity::class.java)
        })

        viewModel.profileLoading.observe(viewLifecycleOwner, Observer { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.profileError.observe(viewLifecycleOwner, Observer { error ->
            manageError(error)
        })
    }
}