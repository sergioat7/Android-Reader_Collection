/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/2/2025
 */

package aragones.sergio.readercollection.presentation.bookdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.components.TextFieldAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import com.aragones.sergio.util.extensions.isNotBlank

@Composable
fun BookDetailView(onBack: () -> Unit, viewModel: BookDetailViewModel = hiltViewModel()) {
    val state by viewModel.state
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()
    val imageDialogMessageId by viewModel.imageDialogMessageId.collectAsState()
    val error by viewModel.bookDetailError.collectAsState()

    ReaderCollectionApp(statusBarSameAsBackground = false) {
        BookDetailScreen(
            state = state,
            onBack = onBack,
            onEdit = {
                viewModel.enableEdition()
            },
            onRemove = {
                viewModel.showConfirmationDialog(R.string.book_remove_confirmation)
            },
            onCancel = {
                viewModel.disableEdition()
            },
            onSave = {
                if (state.isAlreadySaved) {
                    viewModel.setBook(it)
                } else {
                    viewModel.createBook(it)
                }
            },
            onChangeData = viewModel::changeData,
            onSetImage = {
                viewModel.showImageDialog(R.string.enter_valid_url)
            },
        )
    }

    ConfirmationAlertDialog(
        show = confirmationMessageId != -1,
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            viewModel.closeDialogs()
            viewModel.deleteBook()
        },
    )

    val text = if (error != null) {
        val errorText = StringBuilder()
        if (requireNotNull(error).error.isNotEmpty()) {
            errorText.append(requireNotNull(error).error)
        } else {
            errorText.append(stringResource(requireNotNull(error).errorKey))
        }
        errorText.toString()
    } else if (infoDialogMessageId != -1) {
        stringResource(infoDialogMessageId)
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
        if (error != null) onBack()
    }

    TextFieldAlertDialog(
        show = imageDialogMessageId != -1,
        titleTextId = imageDialogMessageId,
        type = KeyboardType.Uri,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            viewModel.closeDialogs()
            if (it.isNotBlank()) viewModel.setBookImage(it)
        },
    )

    LaunchedEffectOnce {
        viewModel.onCreate()
    }
}