package com.example.intercambios.utils

import android.content.Context
import com.example.intercambios.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object  DateUtils {
    // Cambia el formato de las fechas
    fun dateFormatting(context: Context, date: String): String {
        val meses = context.resources.getStringArray(R.array.meses)
        val anio = date.substring(0, 4)
        val mes = meses[(date.substring(5, 7).toInt()) - 1]
        val dia = date.substring(8)

        return "$dia - $mes - $anio"
    }

    fun isDatePast(dateString: String): Boolean {
        return try {
            // Formato de la fecha
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Parsear la fecha
            val inputDate = LocalDate.parse(dateString, formatter)

            // Comparar con la fecha actual
            inputDate.isBefore(LocalDate.now())
        } catch (e: DateTimeParseException) {
            // Manejo de error si el formato es inválido
            println("Formato de fecha inválido: $dateString")
            false
        }
    }
}