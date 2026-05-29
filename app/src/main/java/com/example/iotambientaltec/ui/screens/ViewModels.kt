package com.example.iotambientaltec.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iotambientaltec.data.mock.MockDataGenerator
import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.QueryResult
import com.example.iotambientaltec.data.remote.FirebaseEnvironmentalRepository
import com.example.iotambientaltec.data.repository.EnvironmentalRepository
import com.example.iotambientaltec.data.repository.MockEnvironmentalRepository
import com.example.iotambientaltec.utils.CsvExporter
import com.example.iotambientaltec.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val variableLabels = mapOf("temperature" to "Temperatura", "humidity" to "Humedad", "wind" to "Viento")
fun unitFor(variable: String) = when (variable) { "temperature" -> "°C"; "humidity" -> "%"; else -> "km/h" }

data class DashboardUiState(
    val loading: Boolean = true,
    val latest: List<EnvironmentalData> = emptyList(),
    val max: QueryResult? = null,
    val min: QueryResult? = null,
    val error: String? = null
)
class DashboardViewModel(private val repository: EnvironmentalRepository) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state
    init { load() }
    fun load() = viewModelScope.launch {
        runCatching {
            val latest = repository.getLatestData().first()
            _state.value = DashboardUiState(false, latest, repository.getHistoricalMax("temperature").first(), repository.getHistoricalMin("temperature").first(), if (latest.isEmpty()) "No hay datos ambientales disponibles." else null)
        }.onFailure { _state.value = DashboardUiState(false, error = it.message ?: "Error desconocido") }
    }
}

enum class QueryType(val label: String) { Hourly("Promedio por hora en un día"), Daily("Promedio por día en rango"), Max("Máximo histórico"), Min("Mínimo histórico") }
data class QueryUiState(val variable: String = "", val startDate: String = DateUtils.daysAgo(3), val endDate: String = DateUtils.today(), val type: QueryType? = null, val loading: Boolean = false, val results: List<QueryResult> = emptyList(), val csv: String = "", val error: String? = null) {
    val isValid: Boolean get() = variable.isNotBlank() && type != null && DateUtils.isRangeValid(startDate, endDate)
}
class QueryViewModel(private val repository: EnvironmentalRepository) : ViewModel() {
    private val _state = MutableStateFlow(QueryUiState())
    val state: StateFlow<QueryUiState> = _state
    fun setVariable(v: String) = _state.update { it.copy(variable = v, results = emptyList(), csv = "") }
    fun setStartDate(v: String) = _state.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _state.update { it.copy(endDate = v) }
    fun setType(v: QueryType) = _state.update { it.copy(type = v, results = emptyList(), csv = "") }
    fun query() = viewModelScope.launch {
        val s = _state.value
        if (!s.isValid) { _state.update { it.copy(error = validationMessage(it)) }; return@launch }
        _state.update { it.copy(loading = true, error = null) }
        val results = when (s.type) {
            QueryType.Hourly -> repository.getAverageByHour(s.startDate, s.variable).first()
            QueryType.Daily -> repository.getAverageByDay(s.startDate, s.endDate, s.variable).first()
            QueryType.Max -> listOfNotNull(repository.getHistoricalMax(s.variable).first())
            QueryType.Min -> listOfNotNull(repository.getHistoricalMin(s.variable).first())
            null -> emptyList()
        }
        _state.update { it.copy(loading = false, results = results, csv = CsvExporter.queryResultsToCsv(results), error = if (results.isEmpty()) "No se encontraron resultados." else null) }
    }
    private fun validationMessage(s: QueryUiState) = when {
        s.variable.isBlank() -> "Seleccione una variable ambiental."
        s.type == null -> "Seleccione un tipo de consulta."
        !DateUtils.isRangeValid(s.startDate, s.endDate) -> "La fecha final no puede ser menor que la fecha inicial."
        else -> null
    }
}

data class ChartsUiState(val data: List<EnvironmentalData> = emptyList(), val variable: String = "temperature", val startDate: String = DateUtils.daysAgo(400), val endDate: String = DateUtils.today())
class ChartsViewModel(private val repository: EnvironmentalRepository) : ViewModel() {
    private val _state = MutableStateFlow(ChartsUiState())
    val state: StateFlow<ChartsUiState> = _state
    
    init {
        // Escucha constante: cualquier cambio en Firebase actualiza la UI al instante
        viewModelScope.launch {
            repository.getAllData().collect { allData ->
                android.util.Log.d("CHARTS_DEBUG", "Nueva emisión de datos: ${allData.size} items")
                _state.update { it.copy(data = allData) }
            }
        }
    }

    fun setVariable(v: String) {
        _state.update { it.copy(variable = v) }
    }
}

class MapViewModel : ViewModel() { val points = MockDataGenerator.locationPoints() }

class AppViewModelFactory(private val repository: EnvironmentalRepository = FirebaseEnvironmentalRepository()) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        DashboardViewModel::class.java -> DashboardViewModel(repository)
        QueryViewModel::class.java -> QueryViewModel(repository)
        ChartsViewModel::class.java -> ChartsViewModel(repository)
        MapViewModel::class.java -> MapViewModel()
        else -> error("ViewModel no soportado: $modelClass")
    } as T
}
