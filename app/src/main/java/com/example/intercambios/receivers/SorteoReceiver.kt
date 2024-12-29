package com.example.intercambios.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.intercambios.R
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SorteoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val docId = intent?.getStringExtra("docId") ?: "nada"
        if(docId == "nada"){
            return
        }

        Log.d("SortManager", "Alarma recibida para: $docId")


        // Obtener intercambio y realizar sorteo
        val intercambioUtils = IntercambioRepository()
        intercambioUtils.obtenerIntercambioPorId(docId)
            .addOnSuccessListener { intercambio ->
                if (intercambio.sorteo) {
                    mostrarNotificacion(context, "Sorteo ya realizado", "El sorteo para el intercambio ya se había completado.")
                } else {
                    // Lógica del sorteo basada en tu función
                    val listaParticipantes = intercambio.participantes.toMutableList()
                    val participantesAsignados = mutableListOf<Participante>()

                    val listaParticipantesCopia = intercambio.participantes.toMutableList()

                    listaParticipantes.forEach { participante ->
                        val disponiblesParaAsignar = listaParticipantesCopia.filter { it.uid != participante.uid }
                        val asignado = disponiblesParaAsignar.random()
                        participantesAsignados.add(participante.copy(asignadoA = asignado.uid))
                        listaParticipantesCopia.remove(asignado)
                    }

                    // Actualizar el intercambio
                    // Crear el objeto actualizado de intercambio
                    val intercambioActualizado = intercambio.copy(
                        participantes = participantesAsignados,
                        sorteo = true // Activar el booleano "sorteo"
                    )

                    // Guardar el intercambio actualizado en Firebase
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = intercambioUtils.actualizarIntercambio(intercambioActualizado, docId)
                        if (result) {
                            mostrarNotificacion(context, context.getString(R.string.resultados_listos_titulo), context.getString(R.string.notif_message_success))
                        } else {
                            mostrarNotificacion(context, context.getString(R.string.error_title), context.getString(R.string.error_al_guardar))
                        }
                    }
                }
            }
            .addOnFailureListener {
                mostrarNotificacion(context, context.getString(R.string.error_title), context.getString(R.string.notif_message_error))
            }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val builder = NotificationCompat.Builder(context, "sorteo_channel")
            .setSmallIcon(R.mipmap.ic_launcher) // Icono de tu app
            .setContentTitle(titulo)
            .setContentText(mensaje) // Texto de vista previa
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje)) // Estilo para texto largo
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }


}
