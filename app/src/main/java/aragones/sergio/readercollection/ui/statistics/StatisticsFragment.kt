/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.statistics

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.databinding.FragmentStatisticsBinding
import aragones.sergio.readercollection.presentation.extensions.getCustomColor
import aragones.sergio.readercollection.presentation.extensions.getCustomFont
import aragones.sergio.readercollection.presentation.extensions.isDarkMode
import aragones.sergio.readercollection.presentation.extensions.style
import aragones.sergio.readercollection.presentation.interfaces.MenuProviderInterface
import aragones.sergio.readercollection.presentation.interfaces.OnItemClickListener
import aragones.sergio.readercollection.ui.base.BindingFragment
import aragones.sergio.readercollection.ui.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.utils.Constants
import com.aragones.sergio.util.State
import com.aragones.sergio.util.StatusBarStyle
import com.aragones.sergio.util.extensions.getMonthNumber
import com.aragones.sergio.util.extensions.toDate
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@AndroidEntryPoint
class StatisticsFragment :
    BindingFragment<FragmentStatisticsBinding>(),
    MenuProviderInterface,
    OnItemClickListener {

    //region Protected properties
    override val menuProviderInterface = this
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var newFileLauncher: ActivityResultLauncher<Intent>
    private val customColors = ArrayList<Int>()
    private var toolbarSequence: TapTargetSequence? = null
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
        binding.composeView.setContent {

            val confirmationMessageId by viewModel.confirmationDialogMessageId.observeAsState(
                initial = -1
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

                        else -> Unit
                    }
                    viewModel.closeDialogs()
                })

            val infoMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)
            val text = if (infoMessageId != -1) {
                getString(infoMessageId)
            } else {
                ""
            }
            InformationAlertDialog(show = infoMessageId != -1, text = text) {
                viewModel.closeDialogs()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        /*
        Must be created with a delay in order to wait for the fragment menu creation,
        otherwise it wouldn't be icons in the toolbar
         */
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            createSequence()
        }
    }

    override fun onStop() {
        super.onStop()

        toolbarSequence?.cancel()
    }

    override fun onResume() {
        super.onResume()

        viewModel.fetchBooks()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

        menu.clear()
        menuInflater.inflate(R.menu.statistics_toolbar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        return when (menuItem.itemId) {
            R.id.action_import -> {

                viewModel.showConfirmationDialog(R.string.import_confirmation)
                true
            }

            R.id.action_export -> {

                viewModel.showConfirmationDialog(R.string.export_confirmation)
                true
            }

            else -> false
        }
    }

    override fun onItemClick(bookId: String) {

        val action =
            StatisticsFragmentDirections.actionStatisticsFragmentToBookDetailFragment(bookId, false)
        findNavController().navigate(action)
    }

    override fun onLoadMoreItemsClick() {}

    override fun onShowAllItemsClick(state: String) {}
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

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
                                context?.contentResolver?.openOutputStream(uri)
                                    ?.use { outputStream ->
                                        outputStream.write(it.toByteArray())
                                        outputStream.close()
                                    }
                            }
                        }
                    }
                }
            }

        customColors.add(requireContext().getCustomColor(R.color.colorPrimary))

        setupBindings()

        binding.barChartBooksByYear.apply {

            isDoubleTapToZoomEnabled = false
            isHighlightPerDragEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                position = XAxisPosition.BOTTOM
                textColor = requireContext().getCustomColor(R.color.textPrimary)
                textSize = resources.getDimension(R.dimen.text_size_4sp)
                typeface = requireContext().getCustomFont(R.font.roboto_serif_thin)
                valueFormatter = NumberValueFormatter()
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            axisRight.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    showBooks(
                        e?.x?.toInt(),
                        null,
                        null,
                        null
                    )
                }

                override fun onNothingSelected() {}
            })
        }

        binding.pieChartBooksByMonth.apply {

            isRotationEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            centerText = resources.getString(R.string.months)
            setCenterTextColor(context.getCustomColor(R.color.textPrimary))
            setCenterTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setCenterTextTypeface(context.getCustomFont(R.font.roboto_serif_regular))
            setDrawCenterText(true)
            setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
            setEntryLabelTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setEntryLabelTypeface(context.getCustomFont(R.font.roboto_serif_thin))
            setExtraOffsets(5f, 10f, 5f, 5f)
            setUsePercentValues(false)
            setHoleColor(Color.TRANSPARENT)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {

                    val month = (e as PieEntry).label.toDate("MMM").getMonthNumber()
                    showBooks(
                        null,
                        month,
                        null,
                        null
                    )
                }

                override fun onNothingSelected() {}
            })
        }

        binding.horizontalBarChartBooksByAuthor.apply {

            isDoubleTapToZoomEnabled = false
            isHighlightPerDragEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                position = XAxisPosition.BOTTOM
                textColor = requireContext().getCustomColor(R.color.textPrimary)
                textSize = resources.getDimension(R.dimen.text_size_4sp)
                typeface = requireContext().getCustomFont(R.font.roboto_serif_thin)
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            axisRight.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            setExtraOffsets(0F, 0F, 20F, 0F)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    showBooks(
                        null,
                        null,
                        viewModel.booksByAuthorStats.value?.keys?.toMutableList()
                            ?.get(e?.x?.toInt() ?: 0),
                        null
                    )
                }

                override fun onNothingSelected() {}
            })
        }

        binding.pieChartBooksByFormat.apply {

            isRotationEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            centerText = resources.getString(R.string.formats)
            setCenterTextColor(context.getCustomColor(R.color.textPrimary))
            setCenterTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setCenterTextTypeface(context.getCustomFont(R.font.roboto_serif_regular))
            setDrawCenterText(true)
            setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
            setEntryLabelTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setEntryLabelTypeface(context.getCustomFont(R.font.roboto_serif_thin))
            setExtraOffsets(5F, 10F, 5F, 5F)
            setUsePercentValues(true)
            setHoleColor(Color.TRANSPARENT)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    showBooks(
                        null,
                        null,
                        null,
                        Constants.FORMATS.first { it.name == (e as? PieEntry)?.label }.id
                    )
                }

                override fun onNothingSelected() {}
            })
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.booksLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.booksError.observe(viewLifecycleOwner) {
            it?.let { manageError(it) }
        }

        viewModel.booksByYearStats.observe(viewLifecycleOwner) { entries ->

            binding.barChartBooksByYear.visibility =
                if (entries.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = BarDataSet(entries, "").apply {
                valueTextColor = requireContext().getCustomColor(R.color.textPrimary)
                valueTextSize = resources.getDimension(R.dimen.text_size_3sp)
                valueTypeface = requireContext().getCustomFont(R.font.roboto_serif_regular)
                valueFormatter = NumberValueFormatter()
                colors = customColors
                highLightColor = requireContext().getCustomColor(R.color.colorTertiary)
                setDrawValues(true)
            }

            val data = BarData(dataSet)

            binding.barChartBooksByYear.apply {
                xAxis.apply {
                    axisMinimum = data.xMin
                    axisMaximum = data.xMax
                }
                this.data = data
                invalidate()
                animateY(1500)
            }
        }

        viewModel.booksByMonthStats.observe(viewLifecycleOwner) {

            binding.pieChartBooksByMonth.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = PieDataSet(it, "").apply {
                sliceSpace = 1F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                valueLineColor = requireContext().getCustomColor(R.color.colorPrimary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueFormatter = NumberValueFormatter()
                colors = customColors
            }

            val data = PieData(dataSet).apply {
                setValueTextColor(requireContext().getCustomColor(R.color.textPrimary))
                setValueTextSize(resources.getDimension(R.dimen.text_size_3sp))
                setValueTypeface(requireContext().getCustomFont(R.font.roboto_serif_regular))
            }

            binding.pieChartBooksByMonth.apply {
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1400, Easing.EaseInOutQuad)
            }
        }

        viewModel.booksByAuthorStats.observe(viewLifecycleOwner) { books ->

            binding.horizontalBarChartBooksByAuthor.visibility =
                if (books.isEmpty()) View.GONE else View.VISIBLE

            val entries = mutableListOf<BarEntry>()
            for ((index, entry) in books.toList().withIndex()) {
                entries.add(
                    BarEntry(
                        index.toFloat(),
                        entry.second.size.toFloat()
                    )
                )
            }

            val dataSet = BarDataSet(entries, "").apply {
                valueTextColor = requireContext().getCustomColor(R.color.textPrimary)
                valueTextSize = resources.getDimension(R.dimen.text_size_3sp)
                valueTypeface = requireContext().getCustomFont(R.font.roboto_serif_regular)
                valueFormatter = NumberValueFormatter()
                colors = customColors
                highLightColor = requireContext().getCustomColor(R.color.colorTertiary)
                setDrawValues(true)
            }

            val data = BarData(dataSet)

            binding.horizontalBarChartBooksByAuthor.apply {
                xAxis.apply {
                    valueFormatter = StringValueFormatter(books)
                    labelCount = books.size
                }
                axisLeft.apply {
                    axisMinimum = 0F
                    axisMaximum = data.yMax
                }
                this.data = data
                invalidate()
                animateY(1500)
            }
        }

        viewModel.shorterBook.observe(viewLifecycleOwner) {

            binding.textViewShorterBookTitle.visibility =
                if (it == null) View.GONE else View.VISIBLE
            binding.layoutShorterBook.apply {
                root.visibility = if (it == null) View.GONE else View.VISIBLE
                book = it
                onItemClickListener = this@StatisticsFragment
                isDarkMode = context.isDarkMode()
            }
        }

        viewModel.longerBook.observe(viewLifecycleOwner) {

            binding.textViewLongerBookTitle.visibility = if (it == null) View.GONE else View.VISIBLE
            binding.layoutLongerBook.apply {
                root.visibility = if (it == null) View.GONE else View.VISIBLE
                book = it
                onItemClickListener = this@StatisticsFragment
                isDarkMode = context.isDarkMode()
            }
        }

        viewModel.booksByFormatStats.observe(viewLifecycleOwner) {

            binding.pieChartBooksByFormat.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = PieDataSet(it, "").apply {
                sliceSpace = 5F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                valueLineColor = requireContext().getCustomColor(R.color.colorPrimary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                colors = customColors
            }

            val data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(binding.pieChartBooksByFormat))
                setValueTextColor(requireContext().getCustomColor(R.color.textPrimary))
                setValueTextSize(resources.getDimension(R.dimen.text_size_4sp))
                setValueTypeface(requireContext().getCustomFont(R.font.roboto_serif_regular))
            }

            binding.pieChartBooksByFormat.apply {
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1500, Easing.EaseInOutQuad)
            }
        }
    }

    private fun showBooks(year: Int?, month: Int?, author: String?, format: String?) {

        val action = StatisticsFragmentDirections.actionStatisticsFragmentToBookListFragment(
            State.READ,
            viewModel.sortParam,
            viewModel.isSortDescending,
            "",
            year ?: -1,
            month ?: -1,
            author,
            format
        )
        findNavController().navigate(action)
    }

    private fun createSequence() {

        if (!viewModel.tutorialShown) {
            toolbarSequence = TapTargetSequence(requireActivity()).apply {
                targets(
                    TapTarget.forToolbarMenuItem(
                        binding.toolbar,
                        binding.toolbar.menu.findItem(R.id.action_import).itemId,
                        resources.getString(R.string.import_file_icon_tutorial_title),
                        resources.getString(R.string.import_file_icon_tutorial_description)
                    ).style(requireContext()).cancelable(true).tintTarget(true),
                    TapTarget.forToolbarMenuItem(
                        binding.toolbar,
                        binding.toolbar.menu.findItem(R.id.action_export).itemId,
                        resources.getString(R.string.export_file_icon_tutorial_title),
                        resources.getString(R.string.export_file_icon_tutorial_description)
                    ).style(requireContext()).cancelable(true).tintTarget(true)
                )
                continueOnCancel(false)
                listener(object : TapTargetSequence.Listener {
                    override fun onSequenceFinish() {
                        viewModel.setTutorialAsShown()
                    }

                    override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}

                    override fun onSequenceCanceled(lastTarget: TapTarget) {}
                })
                start()
            }
        }
    }
    //endregion

    //region NumberValueFormatter
    inner class NumberValueFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }
    //endregion

    //region StringValueFormatter
    inner class StringValueFormatter(private val map: Map<String, List<Any>>) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return if (value < 0 || value > map.size - 1) "" else map.keys.elementAt(value.toInt())
        }
    }
    //endregion
}