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
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.RegisterViewModelFactory
import aragones.sergio.readercollection.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : BindingFragment<FragmentRegisterBinding>() {

    //region Protected properties
    override val hasOptionsMenu = false
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: RegisterViewModel
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUi()
    }

    override fun onResume() {
        super.onResume()

        binding.textInputLayoutUsername.doAfterTextChanged {
            registerDataChanged()
        }
        binding.textInputLayoutPassword.doAfterTextChanged {
            registerDataChanged()
        }
        binding.textInputLayoutConfirmPassword.doAfterTextChanged {
            registerDataChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
    //endregion

    //region Public methods
    fun register() {

        binding.textInputLayoutUsername.textInputEditText.clearFocus()
        binding.textInputLayoutPassword.textInputEditText.clearFocus()
        binding.textInputLayoutConfirmPassword.textInputEditText.clearFocus()
        viewModel.register(
            binding.textInputLayoutUsername.textInputEditText.text.toString(),
            binding.textInputLayoutPassword.textInputEditText.text.toString()
        )
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(application)
        )[RegisterViewModel::class.java]
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

        viewModel.registerFormState.observe(viewLifecycleOwner, {

            val registerState = it ?: return@observe

            with(binding) {
                binding.textInputLayoutUsername.setError("")
                binding.textInputLayoutPassword.setError("")
                binding.textInputLayoutConfirmPassword.setError("")

                if (registerState.usernameError != null) {
                    textInputLayoutUsername.setError(getString(registerState.usernameError))
                }
                if (registerState.passwordError != null) {

                    textInputLayoutPassword.setError(getString(registerState.passwordError))
                    textInputLayoutConfirmPassword.setError(getString(registerState.passwordError))
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
            binding.textInputLayoutUsername.textInputEditText.text.toString(),
            binding.textInputLayoutPassword.textInputEditText.text.toString(),
            binding.textInputLayoutConfirmPassword.textInputEditText.text.toString()
        )
    }
    //endregion
}