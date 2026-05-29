package com.example.iotambientaltec.data.remote

import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.QueryResult
import com.example.iotambientaltec.data.repository.EnvironmentalRepository
import com.example.iotambientaltec.utils.EnvironmentalStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class FirebaseEnvironmentalRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EnvironmentalRepository {
    private val collection = firestore.collection("environmental_data")

    override fun getAllData(): Flow<List<EnvironmentalData>> = callbackFlow {
        android.util.Log.d("FIREBASE_DEBUG", "Iniciando escucha en la colección: environmental_data")
        val listener = firestore.collection("environmental_data")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FIREBASE_DEBUG", "Error en Firestore: ${error.message}", error)
                    close(error)
                } else {
                    android.util.Log.d("FIREBASE_DEBUG", "Snapshot recibido. Documentos: ${snapshot?.size()}")
                    val docs = snapshot?.documents?.mapNotNull { doc ->
                        val map = doc.data ?: return@mapNotNull null
                        val rawDate = (map["date"] as? String)?.trim()?.removeSuffix(".") ?: ""
                        
                        // Si no tiene fecha, descartamos para evitar crashes en las gráficas
                        if (rawDate.isBlank()) return@mapNotNull null
                        
                        EnvironmentalData(
                            id = doc.id,
                            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            date = rawDate,
                            time = (map["time"] as? String)?.trim() ?: "",
                            variable = (map["variable"] as? String)?.trim()?.lowercase() ?: "sin_variable",
                            value = (map["value"] as? Number)?.toDouble() ?: 0.0,
                            unit = (map["unit"] as? String) ?: "",
                            location = (map["location"] as? String) ?: "",
                            sensorId = (map["sensorId"] as? String)?.trim() ?: ""
                        )
                    }.orEmpty()
                    trySend(docs.sortedBy { it.timestamp })
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getLatestData(): Flow<List<EnvironmentalData>> = getAllData().map { data -> 
        val latest = data.groupBy { it.variable }.mapNotNull { it.value.maxByOrNull(EnvironmentalData::timestamp) }
        android.util.Log.d("FIREBASE_DEBUG", "getLatestData: ${latest.size} items")
        latest
    }
    
    override fun getDataByDateRange(startDate: String, endDate: String): Flow<List<EnvironmentalData>> = getAllData().map { data -> 
        val filtered = EnvironmentalStats.filterByDateRange(data, startDate, endDate)
        android.util.Log.d("FIREBASE_DEBUG", "getDataByDateRange ($startDate to $endDate): ${filtered.size} items de un total de ${data.size}")
        filtered
    }
    
    override fun getAverageByHour(date: String, variable: String): Flow<List<QueryResult>> = getAllData().map { data -> 
        val avg = EnvironmentalStats.averageByHour(data, date, variable)
        android.util.Log.d("FIREBASE_DEBUG", "getAverageByHour ($date, $variable): ${avg.size} resultados")
        avg
    }
    
    override fun getAverageByDay(startDate: String, endDate: String, variable: String): Flow<List<QueryResult>> = getAllData().map { data -> 
        val avg = EnvironmentalStats.averageByDay(data, startDate, endDate, variable)
        android.util.Log.d("FIREBASE_DEBUG", "getAverageByDay ($startDate to $endDate, $variable): ${avg.size} resultados")
        avg
    }
    override fun getHistoricalMax(variable: String): Flow<QueryResult?> = getAllData().map { EnvironmentalStats.historicalMax(it, variable) }
    override fun getHistoricalMin(variable: String): Flow<QueryResult?> = getAllData().map { EnvironmentalStats.historicalMin(it, variable) }
}
