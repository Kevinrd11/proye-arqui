package com.example.iotambientaltec.data.repository

import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.QueryResult
import kotlinx.coroutines.flow.Flow

interface EnvironmentalRepository {
    fun getAllData(): Flow<List<EnvironmentalData>>
    fun getLatestData(): Flow<List<EnvironmentalData>>
    fun getDataByDateRange(startDate: String, endDate: String): Flow<List<EnvironmentalData>>
    fun getAverageByHour(date: String, variable: String): Flow<List<QueryResult>>
    fun getAverageByDay(startDate: String, endDate: String, variable: String): Flow<List<QueryResult>>
    fun getHistoricalMax(variable: String): Flow<QueryResult?>
    fun getHistoricalMin(variable: String): Flow<QueryResult?>
}
