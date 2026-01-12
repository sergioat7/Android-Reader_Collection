/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2025
 */

package aragones.sergio.readercollection.presentation.statistics

import android.content.Context
import android.graphics.Color
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.LocalLanguage
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.components.VerticalBookItem
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants as UtilConstants
import com.aragones.sergio.util.extensions.getMonthNumber
import com.aragones.sergio.util.extensions.toLocalDate
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.export_data
import reader_collection.app.generated.resources.formats
import reader_collection.app.generated.resources.ic_file_export
import reader_collection.app.generated.resources.ic_file_import
import reader_collection.app.generated.resources.import_data
import reader_collection.app.generated.resources.longer_book
import reader_collection.app.generated.resources.months
import reader_collection.app.generated.resources.shorter_book
import reader_collection.app.generated.resources.title_books_count
import reader_collection.app.generated.resources.title_stats

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
        title = stringResource(Res.string.title_stats),
        modifier = modifier.shadow(elevation),
        subtitle = pluralStringResource(Res.plurals.title_books_count, booksRead, booksRead),
        backgroundColor = MaterialTheme.colorScheme.background,
        actions = {
            TopAppBarIcon(
                accessibilityPainter = painterResource(Res.drawable.ic_file_import)
                    .withDescription(stringResource(Res.string.import_data)),
                onClick = onImportClick,
            )
            TopAppBarIcon(
                accessibilityPainter = painterResource(Res.drawable.ic_file_export)
                    .withDescription(stringResource(Res.string.export_data)),
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
    if (state.booksByYearEntries.entries.isNotEmpty()) {
        BooksByYear(
            entries = state.booksByYearEntries,
            onYearSelected = { onGroupClick(it, null, null, null) },
        )
    }
    if (state.booksByMonthEntries.entries.isNotEmpty()) {
        BooksByMonth(
            entries = state.booksByMonthEntries,
            onMonthSelected = { onGroupClick(null, it, null, null) },
        )
    }
    if (state.booksByAuthorStats.entries.isNotEmpty()) {
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
    if (state.booksByFormatEntries.entries.isNotEmpty()) {
        BooksByFormat(
            entries = state.booksByFormatEntries,
            onFormatSelected = { onGroupClick(null, null, null, it) },
        )
    }
}

@Composable
private fun BooksByYear(entries: Entries, onYearSelected: (Int?) -> Unit) {
    val barEntries = entries.entries.map {
        BarEntry(
            it.key.toFloat(),
            it.size.toFloat(),
        )
    }
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = BarDataSet(barEntries, "").apply {
                valueTextColor = context.getCustomColor(R.color.textPrimary)
                valueTextSize = 12.sp.value
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
private fun BooksByMonth(entries: Entries, onMonthSelected: (Int?) -> Unit) {
    val pieEntries = entries.entries.map {
        PieEntry(
            it.size.toFloat(),
            it.key,
        )
    }
    val language = LocalLanguage.current
    val monthsTitle = stringResource(Res.string.months)
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = PieDataSet(pieEntries, "").apply {
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
            }
            PieChart(context).apply {
                isRotationEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                centerText = monthsTitle
                setCenterTextColor(context.getCustomColor(R.color.textPrimary))
                setCenterTextSize(14.sp.value)
                setDrawCenterText(true)
                setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
                setEntryLabelTextSize(14.sp.value)
                setExtraOffsets(5f, 10f, 5f, 5f)
                setUsePercentValues(false)
                setHoleColor(Color.TRANSPARENT)
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        val month = (e as PieEntry)
                            .label
                            .toLocalFormattedDate(language)
                            .getMonthNumber()
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
private fun BooksByAuthor(entries: MapEntries, onAuthorSelected: (String?) -> Unit) {
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val barEntries = mutableListOf<BarEntry>()
            for ((index, entry) in entries.entries.toList().withIndex()) {
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
                        val author = entries.entries.keys.toMutableList()[e?.x?.toInt() ?: 0]
                        onAuthorSelected(author)
                    }

                    override fun onNothingSelected() {}
                })
                xAxis.apply {
                    valueFormatter = StringValueFormatter(entries.entries)
                    labelCount = entries.entries.size
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
                    text = stringResource(Res.string.shorter_book),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
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
                    text = stringResource(Res.string.longer_book),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
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
private fun BooksByFormat(entries: Entries, onFormatSelected: (String?) -> Unit) {
    val pieEntries = entries.entries.map {
        PieEntry(
            it.size.toFloat(),
            it.key,
        )
    }
    val formatsTitle = stringResource(Res.string.formats)
    Spacer(Modifier.height(24.dp))
    AndroidView(
        factory = { context ->
            val customColors = arrayListOf(context.getCustomColor(R.color.colorPrimary))
            val dataSet = PieDataSet(pieEntries, "").apply {
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
            }
            pieChart.apply {
                isRotationEnabled = false
                legend.isEnabled = false
                description.isEnabled = false
                centerText = formatsTitle
                setCenterTextColor(context.getCustomColor(R.color.textPrimary))
                setCenterTextSize(14.sp.value)
                setDrawCenterText(true)
                setEntryLabelColor(context.getCustomColor(R.color.textTertiary))
                setEntryLabelTextSize(14.sp.value)
                setExtraOffsets(5F, 10F, 5F, 5F)
                setUsePercentValues(true)
                setHoleColor(Color.TRANSPARENT)
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        onFormatSelected(
                            FORMATS.first { it.name == (e as? PieEntry)?.label }.id,
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

@CustomPreviewLightDark
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
                booksByYearEntries = Entries(
                    listOf(
                        Entry("2023", 10),
                        Entry("2024", 20),
                    ),
                ),
                booksByMonthEntries = Entries(
                    listOf(
                        Entry("FEB", 10),
                        Entry("AGO", 20),
                    ),
                ),
                booksByAuthorStats = MapEntries(
                    mapOf(
                        "Author 1" to listOf(book),
                        "Author 1" to listOf(book, book),
                    ),
                ),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = Entries(
                    listOf(
                        Entry("Physical", 10),
                        Entry("Digital", 20),
                    ),
                ),
                isLoading = false,
            ),
            StatisticsUiState.Success(
                totalBooksRead = 12345,
                booksByYearEntries = Entries(),
                booksByMonthEntries = Entries(),
                booksByAuthorStats = MapEntries(),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = Entries(),
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

private fun String.toLocalFormattedDate(language: String): LocalDate? {
    val locale = Locale.forLanguageTag(language)
    return try {
        val date = SimpleDateFormat("MMM", locale)
            .apply {
                timeZone = TimeZone.getDefault()
            }.parse(this)
        date?.let {
            SimpleDateFormat(UtilConstants.DATE_FORMAT, locale).format(date).toLocalDate()
        }
    } catch (_: Exception) {
        null
    }
}
