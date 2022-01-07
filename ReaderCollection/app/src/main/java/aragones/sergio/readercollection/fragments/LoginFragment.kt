/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
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
import aragones.sergio.readercollection.activities.RegisterActivity
import aragones.sergio.readercollection.extensions.afterTextChanged
import aragones.sergio.readercollection.extensions.showOrHidePassword
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentLoginBinding
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import aragones.sergio.readercollection.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment: BindingFragment<FragmentLoginBinding>() {

    //region Private properties
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var ibPassword: ImageButton
    private lateinit var btLogin: Button
    private lateinit var btRegister: Button
    private lateinit var viewModel: LoginViewModel
    //endregion

    //region Lifecycle methods
    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Private methods
    private fun initializeUI() {

        val application = activity?.application ?: return
        etUsername = edit_text_username
        etPassword = edit_text_password
        ibPassword = image_button_password
        btLogin = button_login
        btRegister = button_register
        viewModel = ViewModelProvider(this, LoginViewModelFactory(application))[LoginViewModel::class.java]
        setupBindings()

        etUsername.setText(viewModel.username)

        etUsername.afterTextChanged {
            loginDataChanged()
        }

        etPassword.afterTextChanged {
            loginDataChanged()
        }

        ibPassword.setOnClickListener {
            etPassword.showOrHidePassword(ibPassword)
        }

        btLogin.setOnClickListener {

            viewModel.login(
                etUsername.text.toString(),
                etPassword.text.toString()
            )
        }

        btRegister.setOnClickListener {
            launchActivity(RegisterActivity::class.java)
        }
    }

    private fun setupBindings() {

        viewModel.loginFormState.observe(viewLifecycleOwner, {

            val loginState = it ?: return@observe

            btLogin.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                etUsername.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                etPassword.error = getString(loginState.passwordError)
            }
        })

        viewModel.loginLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.loginError.observe(viewLifecycleOwner, { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java)
            } else {

                hideLoading()
                manageError(error)
            }
        })
    }

    private fun loginDataChanged() {

        viewModel.loginDataChanged(
            etUsername.text.toString(),
            etPassword.text.toString()
        )
    }
    //endregion
}