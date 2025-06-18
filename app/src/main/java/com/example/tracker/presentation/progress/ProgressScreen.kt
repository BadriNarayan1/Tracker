package com.example.tracker.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = FakeProgressViewModel()) {
    val selectedCategory by viewModel.selectedCategory
    val selectedRange by viewModel.selectedTimeRange
    val chartMode by viewModel.chartMode
    val avgEffort by viewModel.averageEffort

    Column(
        modifier = Modifier
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
        LineChart(
            linesChartData =  listOf(when (chartMode) {
                ChartMode.TIME -> viewModel.getTimeChartDataTehras()
                ChartMode.EFFORT -> viewModel.getEffortChartDataTehras()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Average Effort (only when showing Effort chart)
        if (chartMode == ChartMode.EFFORT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Avg. Effort", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (it < avgEffort.toInt()) Color.Black else Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("${String.format("%.1f", avgEffort)}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pie Chart (only for All Categories)
// Pie Chart (only for All Categories)
        if (selectedCategory == "All") {
            Text(
                "Time Spent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PieChart(
                pieChartData = viewModel.getPieChartDataTehras(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 Add manual legend
            val labels = listOf("Work", "Study", "Exercise")
            val colors = listOf(Color(0xFF3F51B5), Color(0xFFFF5722), Color(0xFF4CAF50))

            Column {
                labels.zip(colors).forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .padding(end = 8.dp)
                                .background(color = color, shape = MaterialTheme.shapes.small)
                        )
                        Text(text = label)
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

    fun onCategorySelected(cat: String)
    fun onTimeRangeSelected(range: TimeRange)
    fun onChartModeChanged(mode: ChartMode)

    fun getTimeChartDataTehras(): LineChartData
    fun getEffortChartDataTehras(): LineChartData
    fun getPieChartDataTehras(): PieChartData
}

@Composable
fun FakeProgressViewModel(): ProgressViewModel {
    return object : ProgressViewModel {
        override val availableCategories = listOf("All", "Work", "Study", "Exercise")
        override val selectedCategory = remember {mutableStateOf("All")}
        override val selectedTimeRange = remember {mutableStateOf(TimeRange.MONTH)}
        override val chartMode = remember {mutableStateOf(ChartMode.TIME)}
        override val averageEffort = remember {mutableStateOf(4.0f)}

        override fun onCategorySelected(cat: String) { selectedCategory.value = cat }
        override fun onTimeRangeSelected(range: TimeRange) { selectedTimeRange.value = range }
        override fun onChartModeChanged(mode: ChartMode) { chartMode.value = mode }

        override fun getTimeChartDataTehras() = LineChartData(
            points = listOf(
                LineChartData.Point(1f, "Mon"),
                LineChartData.Point(2f, "Tue"),
                LineChartData.Point(1.5f, "Wed"),
                LineChartData.Point(2.8f, "Thu"),
                LineChartData.Point(1.2f, "Fri")
            ),
            lineDrawer = SolidLineDrawer()
        )

        override fun getEffortChartDataTehras() = LineChartData(
            points = listOf(
                LineChartData.Point(3f, "Mon"),
                LineChartData.Point(4f, "Tue"),
                LineChartData.Point(2f, "Wed"),
                LineChartData.Point(5f, "Thu"),
                LineChartData.Point(4.5f, "Fri")
            ),
            lineDrawer = SolidLineDrawer()
        )

        override fun getPieChartDataTehras() = PieChartData(
            slices = listOf(
                PieChartData.Slice(20f, Color(0xFF3F51B5) ),
                PieChartData.Slice(30f, Color(0xFFFF5722)),
                PieChartData.Slice(50f, Color(0xFF4CAF50))
            )
        )
    }
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


@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    ProgressScreen(viewModel = FakeProgressViewModel())
}
