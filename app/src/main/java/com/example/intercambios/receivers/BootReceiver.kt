package com.example.intercambios.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.utils.SortManager.configurarAlarmaSorteo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val intercambioUtils = IntercambioRepository()
            val userId = Firebase.auth.currentUser?.uid
            if(userId != null) {
                intercambioUtils.obtenerIntercambios().addOnSuccessListener { intercambios ->
                    for ((intercambio, documentId) in intercambios) {
                        if(!intercambio.sorteo && intercambio.organizador == userId){
                            configurarAlarmaSorteo(context, intercambio.fechaMaxRegistro,documentId)
                        }
                    }
                }
            }
        }
    }

}
