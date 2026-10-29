package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun todayDateString(): String {
        return isoFormat.format(Date())
    }

    fun currentMonthKey(): String {
        return monthKeyFormat.format(Date())
    }

    fun formatDisplayDate(dateString: String): String {
        return try {
            val date = isoFormat.parse(dateString) ?: Date()
            displayFormat.format(date)
        } catch (_: Exception) {
            dateString
        }
    }

    fun formatMonthDisplay(monthKey: String): String {
        return try {
            val date = monthKeyFormat.parse(monthKey) ?: Date()
            monthDisplayFormat.format(date)
        } catch (_: Exception) {
            monthKey
        }
    }

    fun getDayOfWeek(dateString: String): String {
        return try {
            val date = isoFormat.parse(dateString) ?: Date()
            dayNameFormat.format(date)
        } catch (_: Exception) {
            ""
        }
    }

    fun shiftDate(dateString: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            val date = isoFormat.parse(dateString) ?: Date()
            cal.time = date
            cal.add(Calendar.DAY_OF_MONTH, days)
            isoFormat.format(cal.time)
        } catch (_: Exception) {
            dateString
        }
    }

    fun shiftMonth(monthKey: String, months: Int): String {
        return try {
            val cal = Calendar.getInstance()
            val date = monthKeyFormat.parse(monthKey) ?: Date()
            cal.time = date
            cal.add(Calendar.MONTH, months)
            monthKeyFormat.format(cal.time)
        } catch (_: Exception) {
            monthKey
        }
    }

    fun getDaysInMonth(monthKey: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val cal = Calendar.getInstance()
            val date = monthKeyFormat.parse(monthKey) ?: Date()
            cal.time = date
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (d in 1..maxDay) {
                cal.set(Calendar.DAY_OF_MONTH, d)
                list.add(isoFormat.format(cal.time))
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }

    fun parseYearMonth(monthKey: String): Pair<Int, Int> {
        return try {
            val parts = monthKey.split("-")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (_: Exception) {
            val cal = Calendar.getInstance()
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
    }
}
