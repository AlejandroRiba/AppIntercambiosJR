package com.example.intercambios.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.intercambios.receivers.SorteoReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SortManager {

    fun configurarAlarmaSorteo(context: Context, fecha: String, docId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SorteoReceiver::class.java).apply {
            putExtra("docId", docId)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, docId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Convertir la fecha recibida en un tiempo en milisegundos
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaMillis = formato.parse(fecha)?.time ?: return


        // Crear un Calendar para la fecha recibida
        val fechaProgramada = Calendar.getInstance().apply {
            timeInMillis = fechaMillis
        }

        // Crear un Calendar para la fecha actual
        val ahora = Calendar.getInstance()

        // Ajustar triggerAtMillis dependiendo de si la fecha ya pasó
        val triggerAtMillis: Long = if (fechaProgramada.get(Calendar.YEAR) == ahora.get(Calendar.YEAR) &&
            fechaProgramada.get(Calendar.DAY_OF_YEAR) == ahora.get(Calendar.DAY_OF_YEAR) &&
            fechaMillis <= System.currentTimeMillis()
        ) {
            Log.d("SortManager", "Fecha ya pasó pero es hoy. Reprogramando a 2 minutos desde ahora.")
            ahora.add(Calendar.MINUTE, 1)
            ahora.timeInMillis
        } else {
            // Día siguiente a las 00:00 hrs
            fechaMillis + AlarmManager.INTERVAL_DAY
        }

        // Crear un objeto SimpleDateFormat para formatear la fecha
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Convertir triggerAtMillis a una fecha
        val fechaFormateada = formatoFecha.format(Date(triggerAtMillis))

        // Imprimir el log con la fecha y hora formateada
        Log.d("SortManager", "Alarma programada para: $fechaFormateada")

        // Validar si la fecha ya ha pasado
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w("SortManager", "No se puede programar el sorteo, la fecha ya expiró.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Redirige al usuario a la configuración para habilitarlo
                val intentperms = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intentperms)
                Log.d("SortManager", "Sorteo programado")
            }
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.d("SortManager", "Sorteo  $fecha - $docId creado")
    }

    fun cancelarAlarmaSorteo(context: Context, docId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SorteoReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, docId.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if(pendingIntent != null){
            alarmManager.cancel(pendingIntent)
            Log.d("SortManager", "Sorteo programado $docId cancelado")
        }else{
            Log.d("SortManager", "Sorteo programado $docId no existía")
        }


    }




}