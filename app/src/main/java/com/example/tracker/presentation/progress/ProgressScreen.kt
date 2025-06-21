package com.example.tracker.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import kotlin.math.ceil

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val selectedCategory by viewModel.selectedCategory
    val selectedRange by viewModel.selectedTimeRange
    val chartMode by viewModel.chartMode
    val avgEffort by viewModel.averageEffort
    val lineChartData by viewModel.lineChartData
    val pieChartData by viewModel.pieChartData
    val pieSlices by viewModel.pieSliceInfo

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filters
        FilterSection(
            categories = viewModel.availableCategories,
            selectedCategory = selectedCategory,
            onCategoryChange = viewModel::onCategorySelected,
            selectedRange = selectedRange,
            onRangeChange = viewModel::onTimeRangeSelected,
            chartMode = chartMode,
            onChartModeChange = viewModel::onChartModeChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Line Chart
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
            LineChart(
                linesChartData = listOf(lineChartData),
                modifier = Modifier
                    .width(600.dp) // Wider for better readability
                    .height(320.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Average Effort (only when showing Effort chart)
//        if (chartMode == ChartMode.EFFORT) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("Avg. Effort", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.width(8.dp))
//                repeat(5) {
//                    Icon(
//                        Icons.Default.Star,
//                        contentDescription = null,
//                        tint = if (it < avgEffort.toInt()) Color.Black else Color.LightGray
//                    )
//                }
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("${String.format("%.1f", avgEffort)}")
//            }
//        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pie Chart (only for All Categories)
        if (selectedCategory == "All") {
            Text(
                text = if (chartMode == ChartMode.TIME) "Time Spent" else "Effort Spent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PieChart(
                pieChartData = pieChartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val total = pieSlices.sumOf { it.value.toDouble() }.coerceAtLeast(1.0)

            Column {
                pieSlices.forEach { slice ->
                    val percentage = (slice.value / total * 100).toInt()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(slice.color, shape = MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${slice.label}: ${ceil(slice.value).toInt()} hr ($percentage%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }


        }
    }
}

@Composable
fun FilterSection(
    categories: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedRange: TimeRange,
    onRangeChange: (TimeRange) -> Unit,
    chartMode: ChartMode,
    onChartModeChange: (ChartMode) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryDropdown(
                categories = categories,
                selected = selectedCategory,
                onCategorySelected = onCategoryChange,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TimeRangeToggle(selectedRange, onRangeChange)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ChartModeToggle(chartMode, onChartModeChange)
    }
}

@Composable
fun TimeRangeToggle(selected: TimeRange, onSelect: (TimeRange) -> Unit) {
    val options = TimeRange.values().toList()
    Row {
        options.forEach {
            Button(
                onClick = { onSelect(it) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (it == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(it.name.lowercase().replaceFirstChar { c -> c.uppercase() })
            }
        }
    }
}

@Composable
fun ChartModeToggle(selected: ChartMode, onChange: (ChartMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        listOf(ChartMode.TIME, ChartMode.EFFORT).forEach { mode ->
            Button(
                onClick = { onChange(mode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(mode.name.lowercase().replaceFirstChar { c -> c.uppercase() })
            }
        }
    }
}

enum class TimeRange { WEEK, MONTH, YEAR }
enum class ChartMode { TIME, EFFORT }

interface ProgressViewModel {
    val availableCategories: List<String>
    val selectedCategory: State<String>
    val selectedTimeRange: State<TimeRange>
    val chartMode: State<ChartMode>
    val averageEffort: State<Float>
    val lineChartData: State<LineChartData>
    val pieChartData: State<PieChartData>
    val pieSliceInfo: State<List<PieSliceInfo>>

    fun onCategorySelected(cat: String)
    fun onTimeRangeSelected(range: TimeRange)
    fun onChartModeChanged(mode: ChartMode)

}




@Composable
fun CategoryDropdown(
    categories: List<String>,
    selected: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun ProgressScreenPreview() {
    // Provide a mocked view model or skip preview when not needed
}