package com.example.appconteo.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Fechas {
    fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    fun horaActual(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    fun conOffset(dias: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dias) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}
