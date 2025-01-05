/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.statistics

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentStatisticsBinding
import aragones.sergio.readercollection.presentation.ui.base.BindingFragment
import aragones.sergio.readercollection.presentation.ui.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.StatusBarStyle
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@AndroidEntryPoint
class StatisticsFragment : BindingFragment<FragmentStatisticsBinding>() {

    //region Protected properties
    override val menuProviderInterface = null
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var newFileLauncher: ActivityResultLauncher<Intent>
    //endregion

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchBooks()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ReaderCollectionTheme {
                val state by viewModel.state
                val error by viewModel.booksError.observeAsState()
                val confirmationMessageId by viewModel.confirmationDialogMessageId.observeAsState(
                    initial = -1,
                )
                val infoMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)

                StatisticsScreen(
                    state = state,
                    onImportClick = {
                        viewModel.showConfirmationDialog(R.string.import_confirmation)
                    },
                    onExportClick = {
                        viewModel.showConfirmationDialog(R.string.export_confirmation)
                    },
                    onGroupClick = { year, month, author, format ->
                        val action = StatisticsFragmentDirections
                            .actionStatisticsFragmentToBookListFragment(
                                BookState.READ,
                                viewModel.sortParam,
                                viewModel.isSortDescending,
                                "",
                                year ?: -1,
                                month ?: -1,
                                author,
                                format,
                            )
                        findNavController().navigate(action)
                    },
                    onBookClick = { bookId ->
                        val action =
                            StatisticsFragmentDirections
                                .actionStatisticsFragmentToBookDetailFragment(
                                    bookId,
                                    false,
                                )
                        findNavController().navigate(action)
                    },
                )

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
                        errorText.append(resources.getString(requireNotNull(error).errorKey))
                    }
                    errorText.toString()
                } else if (infoMessageId != -1) {
                    getString(infoMessageId)
                } else {
                    ""
                }
                InformationAlertDialog(show = infoMessageId != -1, text = text) {
                    viewModel.closeDialogs()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->

                        try {
                            val inputStream = context?.contentResolver?.openInputStream(uri)
                            val reader = BufferedReader(InputStreamReader(inputStream))
                            val jsonData = reader.readLine()
                            inputStream?.close()
                            viewModel.importData(jsonData)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        newFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->

                        viewModel.getDataToExport {
                            it?.let {
                                context
                                    ?.contentResolver
                                    ?.openOutputStream(uri)
                                    ?.use { outputStream ->
                                        outputStream.write(it.toByteArray())
                                        outputStream.close()
                                    }
                            }
                        }
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion
}