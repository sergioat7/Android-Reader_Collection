/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.MainActivity
import aragones.sergio.readercollection.extensions.afterTextChanged
import aragones.sergio.readercollection.extensions.clearErrors
import aragones.sergio.readercollection.extensions.onFocusChange
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.RegisterViewModelFactory
import aragones.sergio.readercollection.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.register_fragment.*

class RegisterFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var ibPassword: ImageButton
    private lateinit var etConfirmPassword: EditText
    private lateinit var ibConfirmPassword: ImageButton
    private lateinit var btRegister: Button
    private lateinit var viewModel: RegisterViewModel

    //MARK: - Lifecycle methods

    companion object {
        fun newInstance() = RegisterFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.register_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeUI()
    }

    //MARK: - Private methods

    private fun initializeUI() {

        val application = activity?.application ?: return
        etUsername = edit_text_username
        etPassword = edit_text_password
        ibPassword = image_button_password
        etConfirmPassword = edit_text_confirm_password
        ibConfirmPassword = image_button_confirm_password
        btRegister = button_register
        viewModel = ViewModelProvider(this, RegisterViewModelFactory(application)).get(
            RegisterViewModel::class.java
        )

        etUsername.afterTextChanged {
            registerDataChanged()
        }
        etUsername.onFocusChange {
            registerDataChanged()
        }

        etPassword.afterTextChanged {
            registerDataChanged()
        }
        etPassword.onFocusChange {
            registerDataChanged()
        }

        ibPassword.setOnClickListener {
            Constants.showOrHidePassword(etPassword, ibPassword)
        }

        etConfirmPassword.afterTextChanged {
            registerDataChanged()
        }
        etConfirmPassword.onFocusChange {
            registerDataChanged()
        }

        ibConfirmPassword.setOnClickListener {
            Constants.showOrHidePassword(etConfirmPassword, ibConfirmPassword)
        }

        btRegister.setOnClickListener {
            register()
        }

        viewModel.registerFormState.observe(requireActivity(), {

            val registerState = it ?: return@observe

            etUsername.clearErrors()
            etPassword.clearErrors()
            etConfirmPassword.clearErrors()

            btRegister.isEnabled = registerState.isDataValid

            if (registerState.usernameError != null) {
                etUsername.error = getString(registerState.usernameError)
            }
            if (registerState.passwordError != null) {

                etPassword.error = getString(registerState.passwordError)
                etConfirmPassword.error = getString(registerState.passwordError)
            }
        })

        viewModel.registerLoading.observe(requireActivity(), { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.registerError.observe(requireActivity(), { error ->

            if (error == null) {
                login()
            } else {
                manageError(error)
            }
        })

        viewModel.loginError.observe(requireActivity(), { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java)
            } else {
                manageError(error)
            }
        })
    }

    private fun registerDataChanged() {

        viewModel.registerDataChanged(
            etUsername.text.toString(),
            etPassword.text.toString(),
            etConfirmPassword.text.toString()
        )
    }

    private fun register() {

        viewModel.register(
            etUsername.text.toString(),
            etPassword.text.toString()
        )
    }

    private fun login() {

        viewModel.login(
            etUsername.text.toString(),
            etPassword.text.toString()
        )
    }
}