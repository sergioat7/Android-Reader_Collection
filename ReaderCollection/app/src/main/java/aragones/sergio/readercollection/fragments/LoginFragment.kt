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
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodelfactories.LoginViewModelFactory
import aragones.sergio.readercollection.viewmodels.LoginViewModel
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var ibPassword: ImageButton
    private lateinit var btLogin: Button
    private lateinit var btRegister: Button
    private lateinit var viewModel: LoginViewModel

    //MARK: - Lifecycle methods

    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_fragment, container, false)
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
        btLogin = button_login
        btRegister = button_register
        viewModel = ViewModelProvider(this, LoginViewModelFactory(application)).get(LoginViewModel::class.java)

        etUsername.setText(viewModel.username)

        etUsername.afterTextChanged {
            loginDataChanged()
        }

        etPassword.afterTextChanged {
            loginDataChanged()
        }

        ibPassword.setOnClickListener {
            Constants.showOrHidePassword(etPassword, ibPassword, Constants.isDarkMode(context))
        }

        btLogin.setOnClickListener {
            login()
        }

        btRegister.setOnClickListener {
            launchActivity(RegisterActivity::class.java)
        }

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

    private fun login() {

        viewModel.login(
            etUsername.text.toString(),
            etPassword.text.toString()
        )
    }
}