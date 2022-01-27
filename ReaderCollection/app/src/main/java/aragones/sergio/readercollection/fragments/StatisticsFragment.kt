/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2022
 */

package aragones.sergio.readercollection.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BindingFragment
import aragones.sergio.readercollection.databinding.FragmentStatisticsBinding
import aragones.sergio.readercollection.extensions.getCustomColor
import aragones.sergio.readercollection.extensions.getCustomFont
import aragones.sergio.readercollection.extensions.isDarkMode
import aragones.sergio.readercollection.utils.StatusBarStyle
import aragones.sergio.readercollection.viewmodelfactories.StatisticsViewModelFactory
import aragones.sergio.readercollection.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class StatisticsFragment : BindingFragment<FragmentStatisticsBinding>() {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: StatisticsViewModel
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Protected methods
    override fun initializeUi() {
        super.initializeUi()

        val application = activity?.application ?: return
        viewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(application)
        )[StatisticsViewModel::class.java]
        setupBindings()

        binding.barChartBooksByYear.apply {

            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                position = XAxisPosition.BOTTOM
                valueFormatter = NumberValueFormatter()
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setPinchZoom(false)
            setFitBars(true)
        }
        binding.pieChartBooksByFormat.apply {

            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            centerText = resources.getString(R.string.formats)
            setUsePercentValues(true)
            setCenterTextColor(requireActivity().getCustomColor(R.color.colorPrimary))
            setCenterTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setCenterTextTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setDrawCenterText(true)
            setEntryLabelColor(requireActivity().getCustomColor(R.color.colorSecondary))
            setEntryLabelTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setEntryLabelTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setExtraOffsets(5F, 10F, 5F, 5F)
            setHoleColor(Color.TRANSPARENT)
        }
    }
    //endregion

    //region Private methods
    private fun setupBindings() {

        viewModel.booksLoading.observe(viewLifecycleOwner, { isLoading ->

            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        })

        viewModel.booksError.observe(viewLifecycleOwner, {
            manageError(it)
        })

        viewModel.booksByYearStats.observe(viewLifecycleOwner, { entries ->

            val colors = ArrayList<Int>()
            colors.add(requireActivity().getCustomColor(R.color.colorPrimary))

            val dataSet = BarDataSet(entries, "").apply {
                valueTextColor = requireActivity().getCustomColor(R.color.colorPrimary)
                valueTextSize = resources.getDimension(R.dimen.text_size_2sp)
                valueFormatter = NumberValueFormatter()
                setDrawValues(true)
                setColors(colors)
            }

            val data = BarData(dataSet)

            binding.barChartBooksByYear.apply {
                xAxis.apply {
                    axisMinimum = data.xMin
                    axisMaximum = data.xMax
                    labelCount = entries.size / 2
                }
                this.data = data
                invalidate()
                animateY(1500)
            }
        })

        viewModel.shorterBook.observe(viewLifecycleOwner, {
            binding.layoutShorterBook.apply {
                book = it
                isDarkMode = requireContext().isDarkMode()
            }
        })

        viewModel.longerBook.observe(viewLifecycleOwner, {
            binding.layoutLongerBook.apply {
                book = it
                isDarkMode = requireContext().isDarkMode()
            }
        })

        viewModel.booksByFormatStats.observe(viewLifecycleOwner, {

            val colors = ArrayList<Int>()
            colors.add(requireActivity().getCustomColor(R.color.colorPrimary))

            val dataSet = PieDataSet(it, "").apply {
                sliceSpace = 5F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                setColors(colors)
            }

            val data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(binding.pieChartBooksByFormat))
                setValueTextColor(requireActivity().getCustomColor(R.color.colorPrimary))
                setValueTextSize(resources.getDimension(R.dimen.text_size_4sp))
                setValueTypeface(requireActivity().getCustomFont(R.font.roboto_bold))
            }

            binding.pieChartBooksByFormat.apply {
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1500, Easing.EaseInOutQuad)
            }
        })
    //region NumberValueFormatter
    inner class NumberValueFormatter : ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }
    //endregion
    }
    //endregion
}