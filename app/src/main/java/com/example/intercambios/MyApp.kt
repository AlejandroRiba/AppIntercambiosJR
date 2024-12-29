package com.example.intercambios

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        crearCanalNotificaciones(this)
    }

    private fun crearCanalNotificaciones(context: Context) {
        val canal = NotificationChannel(
            "sorteo_channel",
            "Sorteos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de resultados del sorteo de intercambios."
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

}