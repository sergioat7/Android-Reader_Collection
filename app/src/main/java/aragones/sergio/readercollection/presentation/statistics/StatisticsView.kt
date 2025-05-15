/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.statistics

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Composable
fun StatisticsView(
    onBookClick: (String) -> Unit,
    onShowAll: (String?, Boolean, Int, Int, String?, String?) -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->

                    try {
                        val inputStream = context.contentResolver?.openInputStream(uri)
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val jsonData = reader.readLine()
                        inputStream?.close()
                        viewModel.importData(jsonData)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        },
    )
    val newFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->

                    viewModel.getDataToExport {
                        it?.let {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(it.toByteArray())
                                outputStream.close()
                            }
                        }
                    }
                }
            }
        },
    )

    val state by viewModel.state
    val error by viewModel.booksError.collectAsState()
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()
    val infoMessageId by viewModel.infoDialogMessageId.collectAsState()

    ReaderCollectionApp(navigationBarSameAsBackground = false) {
        StatisticsScreen(
            state = state,
            onImportClick = {
                viewModel.showConfirmationDialog(R.string.import_confirmation)
            },
            onExportClick = {
                viewModel.showConfirmationDialog(R.string.export_confirmation)
            },
            onGroupClick = { year, month, author, format ->
                onShowAll(
                    viewModel.sortParam,
                    viewModel.isSortDescending,
                    year ?: -1,
                    month ?: -1,
                    author,
                    format,
                )
            },
            onBookClick = onBookClick,
        )
    }

    ConfirmationAlertDialog(
        show = confirmationMessageId != -1,
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            when (confirmationMessageId) {
                R.string.import_confirmation -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
                    openFileLauncher.launch(intent)
                }
                R.string.export_confirmation -> {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/txt"
                        putExtra(Intent.EXTRA_TITLE, "database_backup.txt")
                    }
                    newFileLauncher.launch(intent)
                }
                else -> {
                    Unit
                }
            }
            viewModel.closeDialogs()
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
    } else if (infoMessageId != -1) {
        stringResource(infoMessageId)
    } else {
        ""
    }
    InformationAlertDialog(show = infoMessageId != -1, text = text) {
        viewModel.closeDialogs()
    }

    LaunchedEffectOnce {
        viewModel.fetchBooks()
    }
}