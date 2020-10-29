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
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.fragments.base.BaseFragment
import aragones.sergio.readercollection.viewmodelfactories.RegisterViewModelFactory
import aragones.sergio.readercollection.viewmodels.RegisterViewModel
import kotlinx.android.synthetic.main.register_fragment.*

class RegisterFragment: BaseFragment() {

    //MARK: - Private properties

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
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
        etConfirmPassword = edit_text_confirm_password
        btRegister = button_register
        viewModel = ViewModelProvider(this, RegisterViewModelFactory(application)).get(RegisterViewModel::class.java)
        // TODO: Use the ViewModel
    }
}