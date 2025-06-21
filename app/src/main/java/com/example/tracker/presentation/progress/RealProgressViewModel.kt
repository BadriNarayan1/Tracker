package com.example.tracker.presentation.progress

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.repository.ProgressRepositoryImpl
import com.example.tracker.domain.model.ProgressEntry
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.PieChartData.Slice
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider


data class PieSliceInfo(
    val label: String,
    val value: Float,
    val color: Color
)

class RealProgressViewModel(
    private val repository: ProgressRepositoryImpl
) : ViewModel(), ProgressViewModel {

    private val _lineChartData = mutableStateOf(
        LineChartData(
            points = emptyList(),
            padBy = 5f,
            startAtZero = true,
            lineDrawer = SolidLineDrawer()
        )
    )

    private val _pieSliceInfo = mutableStateOf<List<PieSliceInfo>>(emptyList())
    override val pieSliceInfo: State<List<PieSliceInfo>> = _pieSliceInfo

    override val lineChartData: State<LineChartData> get() = _lineChartData

    private val _pieChartData = mutableStateOf(PieChartData(emptyList()))
    override val pieChartData: State<PieChartData> get() = _pieChartData

    private val _availableCategories = mutableStateOf<List<String>>(emptyList())
    override val availableCategories: List<String> get() = _availableCategories.value

    private val _selectedCategory = mutableStateOf("All")
    override val selectedCategory: State<String> = _selectedCategory

    private val _selectedTimeRange = mutableStateOf(TimeRange.MONTH)
    override val selectedTimeRange: State<TimeRange> = _selectedTimeRange

    private val _chartMode = mutableStateOf(ChartMode.TIME)
    override val chartMode: State<ChartMode> = _chartMode

    private val _averageEffort = mutableStateOf(0f)
    override val averageEffort: State<Float> = _averageEffort

    private var progressData: List<ProgressEntry> = emptyList()
    private val dynamicColorMap = mutableMapOf<String, Color>()
    private var nextColorIndex = 0
    private val colorPalette = listOf(
        Color(0xFF3F51B5), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF9C27B0),
        Color(0xFF009688), Color(0xFFE91E63), Color(0xFFFFC107), Color(0xFF00BCD4),
        Color(0xFF795548), Color(0xFF8BC34A), Color(0xFFFF9800), Color(0xFF607D8B),
        Color(0xFFCDDC39), Color(0xFF673AB7), Color(0xFF2196F3)
    )

    init {
        loadAvailableCategories()
        loadProgress()
    }

    private fun loadAvailableCategories() {
        viewModelScope.launch {
            val types = repository.loadActivityTypes()
            _availableCategories.value = listOf("All") + types
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            progressData = repository.getProgressEntriesForRange(_selectedTimeRange.value)
            computeAverageEffort()
        }
    }

    override fun onCategorySelected(cat: String) {
        _selectedCategory.value = cat
        computeAverageEffort()
    }

    override fun onTimeRangeSelected(range: TimeRange) {
        _selectedTimeRange.value = range
        loadProgress()
    }

    override fun onChartModeChanged(mode: ChartMode) {
        _chartMode.value = mode
        computeAverageEffort()
    }

    private fun computeAverageEffort() {
        if (_chartMode.value == ChartMode.EFFORT) {
            val category = _selectedCategory.value
            val relevant = progressData.mapNotNull {
                if (category == "All") {
                    it.categoryWiseEffortScaledTime.values.sum()
                } else {
                    it.categoryWiseEffortScaledTime[category]
                }
            }
            _averageEffort.value = if (relevant.isNotEmpty()) relevant.average().coerceIn(0.0, 5.0).toFloat() else 0f
        }

        _lineChartData.value = when (_chartMode.value) {
            ChartMode.TIME -> generateTimeChartData()
            ChartMode.EFFORT -> generateEffortChartData()
        }

        _pieChartData.value = generatePieChartData()

    }

    private fun generateTimeChartData(): LineChartData {
        val points = progressData.mapIndexed { index, entry ->
            val y = if (_selectedCategory.value == "All") {
                entry.categoryWiseTime.values.sum()
            } else {
                entry.categoryWiseTime[_selectedCategory.value] ?: 0f
            }
            LineChartData.Point(y, entry.date.takeLast(5)) // use MM-DD format
        }
        return LineChartData(points = points, lineDrawer = SolidLineDrawer())
    }

    private fun generateEffortChartData(): LineChartData {
        val points = progressData.mapIndexed { index, entry ->
            val y = if (_selectedCategory.value == "All") {
                entry.categoryWiseEffortScaledTime.values.sum()
            } else {
                entry.categoryWiseEffortScaledTime[_selectedCategory.value] ?: 0f
            }
            LineChartData.Point(y, entry.date.takeLast(5))
        }
        return LineChartData(points = points, lineDrawer = SolidLineDrawer())
    }

    private fun generatePieChartData(): PieChartData {
        val pieMap = mutableMapOf<String, Float>()

        progressData.forEach { entry ->
            val sourceMap = when (_chartMode.value) {
                ChartMode.TIME -> entry.categoryWiseTime
                ChartMode.EFFORT -> entry.categoryWiseEffortScaledTime
            }

            sourceMap.forEach { (cat, value) ->
                pieMap[cat] = pieMap.getOrDefault(cat, 0f) + value
            }
        }

        val slices = mutableListOf<Slice>()
        val legends = mutableListOf<PieSliceInfo>()

        pieMap.forEach { (cat, value) ->
            val color = dynamicColorMap.getOrPut(cat) {
                val assignedColor = colorPalette[nextColorIndex % colorPalette.size]
                nextColorIndex++
                assignedColor
            }

            slices.add(Slice(value, color))
            legends.add(PieSliceInfo(cat, value, color))
        }

        _pieSliceInfo.value = legends
        return PieChartData(slices = slices)
    }

}

class RealProgressViewModelFactory(
    private val repository: ProgressRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealProgressViewModel::class.java)) {
            return RealProgressViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

