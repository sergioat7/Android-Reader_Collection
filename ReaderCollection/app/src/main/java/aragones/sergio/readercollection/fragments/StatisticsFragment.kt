/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2022
 */

package aragones.sergio.readercollection.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.OnItemClickListener
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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class StatisticsFragment : BindingFragment<FragmentStatisticsBinding>(), OnItemClickListener {

    //region Protected properties
    override val hasOptionsMenu = true
    override val statusBarStyle = StatusBarStyle.PRIMARY
    //endregion

    //region Private properties
    private lateinit var viewModel: StatisticsViewModel
    private val customColors = ArrayList<Int>()
    //endregion

    //region Lifecycle methods
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = binding.toolbar
        initializeUi()
    }

    override fun onResume() {
        super.onResume()

        if (this::viewModel.isInitialized) viewModel.fetchBooks()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized) viewModel.onDestroy()
    }
    //endregion

    //region Interface methods
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

        customColors.add(requireActivity().getCustomColor(R.color.colorPrimary))

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
            setPinchZoom(false)
            setFitBars(true)
        }

        binding.pieChartBooksByMonth.apply {

            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            centerText = resources.getString(R.string.months)
            setCenterTextColor(requireActivity().getCustomColor(R.color.colorPrimary))
            setCenterTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setCenterTextTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setDrawCenterText(true)
            setEntryLabelColor(requireActivity().getCustomColor(R.color.colorSecondary))
            setEntryLabelTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setEntryLabelTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setExtraOffsets(5f, 10f, 5f, 5f)
            setUsePercentValues(false)
            setHoleColor(Color.TRANSPARENT)
        }

        binding.horizontalBarChartBooksByAuthor.apply {

            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                position = XAxisPosition.BOTTOM
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
            setPinchZoom(false)
            setFitBars(true)
        }

        binding.pieChartBooksByFormat.apply {

            isRotationEnabled = false
            isHighlightPerTapEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            centerText = resources.getString(R.string.formats)
            setCenterTextColor(requireActivity().getCustomColor(R.color.colorPrimary))
            setCenterTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setCenterTextTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setDrawCenterText(true)
            setEntryLabelColor(requireActivity().getCustomColor(R.color.colorSecondary))
            setEntryLabelTextSize(resources.getDimension(R.dimen.text_size_4sp))
            setEntryLabelTypeface(requireActivity().getCustomFont(R.font.roboto_regular))
            setExtraOffsets(5F, 10F, 5F, 5F)
            setUsePercentValues(true)
            setHoleColor(Color.TRANSPARENT)
        }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
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

            binding.barChartBooksByYear.visibility =
                if (entries.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = BarDataSet(entries, "").apply {
                valueTextColor = requireActivity().getCustomColor(R.color.colorPrimary)
                valueTextSize = resources.getDimension(R.dimen.text_size_2sp)
                valueFormatter = NumberValueFormatter()
                this.colors = customColors
                setDrawValues(true)
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

        viewModel.booksByMonthStats.observe(viewLifecycleOwner, {

            binding.pieChartBooksByMonth.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = PieDataSet(it, "").apply {
                sliceSpace = 5F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                valueTextColor = requireActivity().getCustomColor(R.color.colorTertiary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueFormatter = NumberValueFormatter()
                this.colors = customColors
            }

            val data = PieData(dataSet).apply {
                setValueTextColor(requireActivity().getCustomColor(R.color.colorPrimary))
                setValueTextSize(resources.getDimension(R.dimen.text_size_4sp))
                setValueTypeface(requireActivity().getCustomFont(R.font.roboto_bold))
            }

            binding.pieChartBooksByMonth.apply {
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1400, Easing.EaseInOutQuad)
            }
        })

        viewModel.booksByAuthorStats.observe(viewLifecycleOwner, { books ->

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
                valueTextColor = requireActivity().getCustomColor(R.color.colorPrimary)
                valueTextSize = resources.getDimension(R.dimen.text_size_2sp)
                valueFormatter = NumberValueFormatter()
                this.colors = customColors
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
        })

        viewModel.shorterBook.observe(viewLifecycleOwner, {

            binding.textViewShorterBookTitle.visibility =
                if (it == null) View.GONE else View.VISIBLE
            binding.layoutShorterBook.apply {
                root.visibility = if (it == null) View.GONE else View.VISIBLE
                book = it
                onItemClickListener = this@StatisticsFragment
                isDarkMode = requireContext().isDarkMode()
            }
        })

        viewModel.longerBook.observe(viewLifecycleOwner, {

            binding.textViewLongerBookTitle.visibility = if (it == null) View.GONE else View.VISIBLE
            binding.layoutLongerBook.apply {
                root.visibility = if (it == null) View.GONE else View.VISIBLE
                book = it
                onItemClickListener = this@StatisticsFragment
                isDarkMode = requireContext().isDarkMode()
            }
        })

        viewModel.booksByFormatStats.observe(viewLifecycleOwner, {

            binding.pieChartBooksByFormat.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

            val dataSet = PieDataSet(it, "").apply {
                sliceSpace = 5F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                colors = customColors
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
            return if(value < 0 || value > map.size - 1) "" else map.keys.elementAt(value.toInt())
        }
    }
    //endregion
}