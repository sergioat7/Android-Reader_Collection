/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.register

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.ActivityRegisterBinding
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.extensions.setStatusBarStyle
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.base.BaseActivity
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    //region Private properties
    private val viewModel: RegisterViewModel by viewModels()
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setStatusBarStyle(
            ContextCompat.getColor(this, R.color.colorSecondary),
            !isDarkMode(),
        )

        ActivityRegisterBinding.inflate(layoutInflater).apply {
            setContentView(root)

            composeView.setContent {
                ReaderCollectionTheme {
                    val state by viewModel.uiState
                    val error by viewModel.registerError.observeAsState()
                    val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(
                        initial = -1,
                    )

                    RegisterScreen(
                        state = state,
                        onShowInfo = { viewModel.showInfoDialog(R.string.username_info) },
                        onRegisterDataChange = viewModel::registerDataChanged,
                        onRegister = viewModel::register,
                    )

                    val text = if (error != null) {
                        val errorText = StringBuilder()
                        if (requireNotNull(error).error.isNotEmpty()) {
                            errorText.append(requireNotNull(error).error)
                        } else {
                            errorText.append(resources.getString(requireNotNull(error).errorKey))
                        }
                        errorText.toString()
                    } else if (infoDialogMessageId != -1) {
                        getString(infoDialogMessageId)
                    } else {
                        ""
                    }
                    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
                        viewModel.closeDialogs()
                    }
                }
            }
        }

        viewModel.activityName.observe(this) { activityName ->

            when (activityName) {
                MainActivity::class.simpleName -> launchActivity(MainActivity::class.java)
                else -> Unit
            }
        }
    }
    //endregion
}