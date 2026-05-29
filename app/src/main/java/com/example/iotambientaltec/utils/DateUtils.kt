package com.example.iotambientaltec.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

    fun today(): String = dateFormat.format(System.currentTimeMillis())
    fun daysAgo(days: Int): String = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.time.let(dateFormat::format)
    fun dateToMillis(date: String): Long = dateFormat.parse(date)?.time ?: 0L
    fun isRangeValid(start: String, end: String): Boolean = dateToMillis(start) <= dateToMillis(end)
    fun formatTime(calendar: Calendar): String = timeFormat.format(calendar.time)
    fun formatDate(calendar: Calendar): String = dateFormat.format(calendar.time)
}
