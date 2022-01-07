/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.activities.MainActivity
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentRegisterBinding
import aragones.sergio.readercollection.extensions.*
import aragones.sergio.readercollection.viewmodelfactories.RegisterViewModelFactory
import aragones.sergio.readercollection.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : BindingFragment<FragmentRegisterBinding>() {

    //region Protected properties
    override val hasOptionsMenu = false
    //endregion

    //region Private properties
    private lateinit var viewModel: RegisterViewModel
    //endregion

    //region Lifecycle methods
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
        viewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(application)
        )[RegisterViewModel::class.java]

        with(binding) {

            editTextUsername.afterTextChanged {
                registerDataChanged()
            }
            editTextUsername.onFocusChange {
                registerDataChanged()
            }

            imageButtonInfo.setOnClickListener {
                showPopupDialog(resources.getString(R.string.username_info))
            }

            editTextPassword.afterTextChanged {
                registerDataChanged()
            }
            editTextPassword.onFocusChange {
                registerDataChanged()
            }

            imageButtonPassword.setOnClickListener {
                editTextPassword.showOrHidePassword(imageButtonPassword)
            }

            editTextConfirmPassword.afterTextChanged {
                registerDataChanged()
            }
            editTextConfirmPassword.onFocusChange {
                registerDataChanged()
            }

            imageButtonConfirmPassword.setOnClickListener {
                editTextConfirmPassword.showOrHidePassword(imageButtonConfirmPassword)
            }

            buttonRegister.setOnClickListener {
                register()
            }
        }

        viewModel.registerFormState.observe(viewLifecycleOwner, {

            val registerState = it ?: return@observe

            with(binding) {
                editTextUsername.clearErrors()
                editTextPassword.clearErrors()
                editTextConfirmPassword.clearErrors()

                buttonRegister.isEnabled = registerState.isDataValid

                if (registerState.usernameError != null) {
                    editTextUsername.error = getString(registerState.usernameError)
                }
                if (registerState.passwordError != null) {

                    editTextPassword.error = getString(registerState.passwordError)
                    editTextConfirmPassword.error = getString(registerState.passwordError)
                }
            }
        })

        viewModel.registerLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.registerError.observe(viewLifecycleOwner, { error ->

            if (error == null) {
                launchActivity(MainActivity::class.java)
            } else {
                manageError(error)
            }
        })
    }

    private fun registerDataChanged() {

        viewModel.registerDataChanged(
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString(),
            binding.editTextConfirmPassword.text.toString()
        )
    }

    private fun register() {

        viewModel.register(
            binding.editTextUsername.text.toString(),
            binding.editTextPassword.text.toString()
        )
    }
    //endregion
}