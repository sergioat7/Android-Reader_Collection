/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2025
 */

package aragones.sergio.readercollection.presentation.statistics

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.components.VerticalBookItem
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import aragones.sergio.readercollection.utils.Constants
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.extensions.getMonthNumber
import com.aragones.sergio.util.extensions.toDate
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
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

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize()) {
        StatisticsToolbar(
            state = state,
            scrollState = scrollState,
            onImportClick = onImportClick,
            onExportClick = onExportClick,
        )
        StatisticsContent(
            state = state,
            scrollState = scrollState,
            onGroupClick = onGroupClick,
            onBookClick = onBookClick,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
    if (state is StatisticsUiState.Success && state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun StatisticsToolbar(
    state: StatisticsUiState,
    scrollState: ScrollState,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val booksRead = when (state) {
        is StatisticsUiState.Empty -> 0
        is StatisticsUiState.Success -> state.totalBooksRead
    }

    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(R.string.title_stats),
        modifier = modifier.shadow(elevation),
        subtitle = pluralStringResource(R.plurals.title_books_count, booksRead, booksRead),
        backgroundColor = MaterialTheme.colorScheme.background,
        actions = {
            TopAppBarIcon(
                icon = R.drawable.ic_file_import,
                onClick = onImportClick,
            )
            TopAppBarIcon(
                icon = R.drawable.ic_file_export,
                onClick = onExportClick,
            )
        },
    )
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    scrollState: ScrollState,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(16.dp))
        when (state) {
            is StatisticsUiState.Empty -> NoResultsComponent()
            is StatisticsUiState.Success -> StatisticsComponent(
                state = state,
                onGroupClick = onGroupClick,
                onBookClick = onBookClick,
            )
        }
    }
}

@Composable
private fun StatisticsComponent(
    state: StatisticsUiState.Success,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
) {
    if (state.booksByYearEntries.isNotEmpty()) {
        BooksByYear(
            entries = state.booksByYearEntries,
            onYearSelected = { onGroupClick(it, null, null, null) },
        )
    }
    if (state.booksByMonthEntries.isNotEmpty()) {
        BooksByMonth(
            entries = state.booksByMonthEntries,
            onMonthSelected = { onGroupClick(null, it, null, null) },
        )
    }
    if (state.booksByAuthorStats.isNotEmpty()) {
        BooksByAuthor(
            entries = state.booksByAuthorStats,
            onAuthorSelected = { onGroupClick(null, null, it, null) },
        )
    }
    BooksByPages(
        shorterBook = state.shorterBook,
        longerBook = state.longerBook,
        onBookClick = onBookClick,
    )
    if (state.booksByFormatEntries.isNotEmpty()) {
        BooksByFormat(
            entries = state.booksByFormatEntries,
            onFormatSelected = { onGroupClick(null, null, null, it) },
        )
    }
}

@Composable
private fun BooksByYear(entries: List<BarEntry>, onYearSelected: (Int?) -> Unit) {
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = BarDataSet(entries, "").apply {
                valueTextColor = context.getCustomColor(R.color.textPrimary)
                valueTextSize = 12.sp.value
                valueTypeface = context.getCustomFont(R.font.roboto_serif_regular)
                valueFormatter = NumberValueFormatter()
                colors = customColors
                highLightColor = context.getCustomColor(R.color.colorTertiary)
                setDrawValues(true)
            }
            val data = BarData(dataSet)
            BarChart(context).apply {
                isDoubleTapToZoomEnabled = false
                isHighlightPerDragEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                xAxis.apply {
                    position = XAxisPosition.BOTTOM
                    textColor = context.getCustomColor(R.color.textPrimary)
                    textSize = 14.sp.value
                    typeface = context.getCustomFont(R.font.roboto_serif_thin)
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
                        onYearSelected(e?.x?.toInt())
                    }

                    override fun onNothingSelected() {}
                })
                xAxis.apply {
                    axisMinimum = data.xMin
                    axisMaximum = data.xMax
                }
                this.data = data
                invalidate()
                animateY(1500)
            }
        },
        modifier = Modifier.height(250.dp).fillMaxWidth(),
    )
}

@Composable
private fun BooksByMonth(entries: List<PieEntry>, onMonthSelected: (Int?) -> Unit) {
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 1F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                valueLineColor = context.getCustomColor(R.color.colorPrimary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                valueFormatter = NumberValueFormatter()
                colors = customColors
            }

            val data = PieData(dataSet).apply {
                setValueTextColor(context.getCustomColor(R.color.textPrimary))
                setValueTextSize(12.sp.value)
                setValueTypeface(context.getCustomFont(R.font.roboto_serif_regular))
            }
            PieChart(context).apply {
                isRotationEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                centerText = resources.getString(R.string.months)
                setCenterTextColor(context.getCustomColor(R.color.textPrimary))
                setCenterTextSize(14.sp.value)
                setCenterTextTypeface(context.getCustomFont(R.font.roboto_serif_regular))
                setDrawCenterText(true)
                setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
                setEntryLabelTextSize(14.sp.value)
                setEntryLabelTypeface(context.getCustomFont(R.font.roboto_serif_thin))
                setExtraOffsets(5f, 10f, 5f, 5f)
                setUsePercentValues(false)
                setHoleColor(Color.TRANSPARENT)
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val month = (e as PieEntry).label.toDate("MMM").getMonthNumber()
                        onMonthSelected(month)
                    }

                    override fun onNothingSelected() {}
                })
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1400, Easing.EaseInOutQuad)
            }
        },
        modifier = Modifier.height(250.dp).fillMaxWidth(),
    )
}

@Composable
private fun BooksByAuthor(entries: Map<String, List<Book>>, onAuthorSelected: (String?) -> Unit) {
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val barEntries = mutableListOf<BarEntry>()
            for ((index, entry) in entries.toList().withIndex()) {
                barEntries.add(
                    BarEntry(
                        index.toFloat(),
                        entry.second.size.toFloat(),
                    ),
                )
            }
            val dataSet = BarDataSet(barEntries, "").apply {
                valueTextColor = context.getCustomColor(R.color.textPrimary)
                valueTextSize = 12.sp.value
                valueTypeface = context.getCustomFont(R.font.roboto_serif_regular)
                valueFormatter = NumberValueFormatter()
                colors = customColors
                highLightColor = context.getCustomColor(R.color.colorTertiary)
                setDrawValues(true)
            }
            val data = BarData(dataSet)
            HorizontalBarChart(context).apply {
                isDoubleTapToZoomEnabled = false
                isHighlightPerDragEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                xAxis.apply {
                    position = XAxisPosition.BOTTOM
                    textColor = context.getCustomColor(R.color.textPrimary)
                    textSize = 14.sp.value
                    typeface = context.getCustomFont(R.font.roboto_serif_thin)
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
                        val author = entries.keys.toMutableList()[e?.x?.toInt() ?: 0]
                        onAuthorSelected(author)
                    }

                    override fun onNothingSelected() {}
                })
                xAxis.apply {
                    valueFormatter = StringValueFormatter(entries)
                    labelCount = entries.size
                }
                axisLeft.apply {
                    axisMinimum = 0F
                    axisMaximum = data.yMax
                }
                this.data = data
                invalidate()
                animateY(1500)
            }
        },
        modifier = Modifier.height(250.dp).fillMaxWidth(),
    )
}

@Composable
private fun BooksByPages(shorterBook: Book?, longerBook: Book?, onBookClick: (String) -> Unit) {
    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        shorterBook?.let { book ->
            Column {
                Text(
                    text = stringResource(R.string.shorter_book),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.description,
                )
                Spacer(Modifier.height(8.dp))
                VerticalBookItem(
                    book = book,
                    isSwitchLeftIconEnabled = false,
                    isSwitchRightIconEnabled = false,
                    onClick = { onBookClick(book.id) },
                    onSwitchToLeft = {},
                    onSwitchToRight = {},
                    onLongClick = {},
                )
            }
        }
        longerBook?.let { book ->
            Column {
                Text(
                    text = stringResource(R.string.longer_book),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.description,
                )
                Spacer(Modifier.height(8.dp))
                VerticalBookItem(
                    book = book,
                    isSwitchLeftIconEnabled = false,
                    isSwitchRightIconEnabled = false,
                    onClick = { onBookClick(book.id) },
                    onSwitchToLeft = {},
                    onSwitchToRight = {},
                    onLongClick = {},
                )
            }
        }
    }
}

@Composable
private fun BooksByFormat(entries: List<PieEntry>, onFormatSelected: (String?) -> Unit) {
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 5F
                valueLinePart1Length = 0.4F
                valueLinePart2Length = 0.8F
                valueLineColor = context.getCustomColor(R.color.colorPrimary)
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                colors = customColors
            }

            val pieChart = PieChart(context)
            val data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(pieChart))
                setValueTextColor(context.getCustomColor(R.color.textPrimary))
                setValueTextSize(14.sp.value)
                setValueTypeface(context.getCustomFont(R.font.roboto_serif_regular))
            }
            pieChart.apply {
                isRotationEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                centerText = resources.getString(R.string.formats)
                setCenterTextColor(context.getCustomColor(R.color.textPrimary))
                setCenterTextSize(14.sp.value)
                setCenterTextTypeface(context.getCustomFont(R.font.roboto_serif_regular))
                setDrawCenterText(true)
                setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
                setEntryLabelTextSize(14.sp.value)
                setEntryLabelTypeface(context.getCustomFont(R.font.roboto_serif_thin))
                setExtraOffsets(5F, 10F, 5F, 5F)
                setUsePercentValues(true)
                setHoleColor(Color.TRANSPARENT)
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        onFormatSelected(
                            Constants.FORMATS.first { it.name == (e as? PieEntry)?.label }.id,
                        )
                    }

                    override fun onNothingSelected() {}
                })
                this.data = data
                highlightValues(null)
                invalidate()
                animateY(1500, Easing.EaseInOutQuad)
            }
        },
        modifier = Modifier.height(250.dp).fillMaxWidth(),
    )
}

@PreviewLightDark
@Composable
private fun StatisticsScreenPreview(
    @PreviewParameter(StatisticsScreenPreviewParameterProvider::class) state: StatisticsUiState,
) {
    ReaderCollectionTheme {
        StatisticsScreen(
            state = state,
            onImportClick = {},
            onExportClick = {},
            onGroupClick = { _, _, _, _ -> },
            onBookClick = {},
        )
    }
}

private class StatisticsScreenPreviewParameterProvider :
    PreviewParameterProvider<StatisticsUiState> {

    private val book = Book(
        id = "1",
        title = "Shortest read book",
        subtitle = null,
        authors = listOf("Author"),
        publisher = null,
        publishedDate = null,
        readingDate = null,
        description = null,
        summary = null,
        isbn = null,
        pageCount = 0,
        categories = null,
        averageRating = 0.0,
        ratingsCount = 0,
        rating = 5.0,
        thumbnail = null,
        image = null,
        format = null,
        state = BookState.READ,
        priority = 0,
    )

    override val values: Sequence<StatisticsUiState>
        get() = sequenceOf(
            StatisticsUiState.Success(
                totalBooksRead = 12345,
                booksByYearEntries = listOf(
                    BarEntry(2023f, 10f),
                    BarEntry(2024f, 20f),
                ),
                booksByMonthEntries = listOf(
                    PieEntry(10f, "FEB"),
                    PieEntry(20f, "AGO"),
                ),
                booksByAuthorStats = mapOf(
                    "Author 1" to listOf(book),
                    "Author 1" to listOf(book, book),
                ),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = listOf(
                    PieEntry(10f, "Physical"),
                    PieEntry(20f, "Digital"),
                ),
                isLoading = false,
            ),
            StatisticsUiState.Success(
                totalBooksRead = 12345,
                booksByYearEntries = listOf(),
                booksByMonthEntries = listOf(),
                booksByAuthorStats = mapOf(),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = listOf(),
                isLoading = true,
            ),
            StatisticsUiState.Empty,
        )
}

private class NumberValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = value.toInt().toString()
}

private class StringValueFormatter(private val map: Map<String, List<Any>>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String =
        if (value < 0 || value > map.size - 1) "" else map.keys.elementAt(value.toInt())
}

private fun Context.getCustomColor(colorId: Int): Int =
    ResourcesCompat.getColor(resources, colorId, null)

private fun Context.getCustomFont(fontId: Int): Typeface? = ResourcesCompat.getFont(this, fontId)
