package com.example.intercambios.data.models

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IntercambioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Función para crear intercambio
    suspend fun addIntercambio(
        intercambio: Intercambio
    ): Boolean = suspendCoroutine { continuation ->

        // Referencia a la subcolección de intercambios del usuario
        val intercambioRef = db.collection("intercambios")
            .document() // Genera un ID automáticamente

        // Guardar el objeto en Firestore
        intercambioRef.set(intercambio)
            .addOnSuccessListener {
                Log.e("CrearIntercambio", "Intercambio guardado exitosamente")
                continuation.resume(true)  // Devuelve true si fue exitoso
            }
            .addOnFailureListener { e ->
                Log.e("CrearIntercambio","Error al guardar el intercambio: ${e.message}")
                continuation.resume(false)  // Devuelve false si hubo un error
            }
    }

    //Función para cachar todos los registros de intercambio
    fun obtenerIntercambios(): Task<List<Pair<Intercambio, String>>> { //citas ordenadas por fecha
        val taskCompletionSource = TaskCompletionSource<List<Pair<Intercambio, String>>>()
        val consulta = db.collection("intercambios")

        consulta.get()
            .addOnSuccessListener { result ->
                val intercambios = mutableListOf<Pair<Intercambio, String>>()
                for (document in result) {
                    val intercambio = document.toObject(Intercambio::class.java) // Mapea el documento a un objeto Intercambio
                    // Filtra si el userId está en la lista de participantes
                    if (intercambio.participantes.any { it.uid == userId }) {
                        // Si el userId es participante, añade el intercambio a la lista
                        intercambios.add(Pair(intercambio, document.id)) // Guarda también el id del documento
                    }
                }
                taskCompletionSource.setResult(intercambios) // Devuelve la lista de citas
            }
            .addOnFailureListener {
                taskCompletionSource.setException(it) // Devuelve la excepción
            }

        return taskCompletionSource.task
    }

}