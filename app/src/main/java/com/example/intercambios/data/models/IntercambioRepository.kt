package com.example.intercambios.data.models

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.firestore.SetOptions
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
                    if (intercambio.participantes.any { it.uid == userId && it.activo }) {
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

    // Función para obtener un intercambio por su ID de documento
    fun obtenerIntercambioPorId(docId: String): Task<Intercambio> {
        val taskCompletionSource = TaskCompletionSource<Intercambio>()
        val consulta = db.collection("intercambios").document(docId) // Consulta usando el docId

        consulta.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Mapea el documento a un objeto Intercambio
                    val intercambio = document.toObject(Intercambio::class.java)
                    intercambio?.let {
                        taskCompletionSource.setResult(it) // Devuelve el objeto Intercambio
                    } ?: run {
                        taskCompletionSource.setException(Exception("Intercambio no encontrado"))
                    }
                } else {
                    taskCompletionSource.setException(Exception("Documento no encontrado"))
                }
            }
            .addOnFailureListener {
                taskCompletionSource.setException(it) // Devuelve la excepción si ocurre un error
            }

        return taskCompletionSource.task
    }

    //obtener la referencia al documento de firebase
    fun obtenerDocId(codigo: String): Task<String> {
        val taskCompletionSource = TaskCompletionSource<String>()
        val consulta = db.collection("intercambios").whereEqualTo("code", codigo).limit(1)

        consulta.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documento = querySnapshot.documents[0]
                    taskCompletionSource.setResult(documento.id) // Devuelve el document id
                } else {
                    taskCompletionSource.setException(Exception("Documento no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    //Función para el dinamic link
    fun generarEnlaceDinamico(intercambioId: String, callback: (String?) -> Unit) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://intercambios.com/intercambio?id=$intercambioId"))
            .setDomainUriPrefix("https://intercambios.page.link") // Configurado en Firebase
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.example.intercambios").build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { result ->
                val shortLink = result.shortLink
                callback(shortLink.toString())
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    //Función para eliminar un intercambio
    fun eliminarIntercambioPorId(docId: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val consulta = db.collection("intercambios").document(docId)

        consulta.delete()
            .addOnSuccessListener {
                taskCompletionSource.setResult(true) // Eliminación exitosa
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception) // Error al eliminar
            }

        return taskCompletionSource.task
    }

    //Función para salir o rechazar un intercambio
    fun eliminarParticipante(docId: String, email: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val intercambioRef = db.collection("intercambios").document(docId)

        intercambioRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val intercambio = document.toObject(Intercambio::class.java)
                    if (intercambio != null) {
                        val participantesActualizados = intercambio.participantes.filterNot {
                            it.email == email && (it.uid == userId || it.uid.isEmpty())
                        }
                        val personasRegistradasActualizadas = intercambio.personasRegistradas -
                                (intercambio.participantes.size - participantesActualizados.size)

                        // Actualiza el intercambio con la lista modificada y personas registradas
                        intercambioRef.update(
                            mapOf(
                                "participantes" to participantesActualizados,
                                "personasRegistradas" to personasRegistradasActualizadas
                            )
                        ).addOnSuccessListener {
                            taskCompletionSource.setResult(true) // Eliminación exitosa
                        }.addOnFailureListener { exception ->
                            taskCompletionSource.setException(exception) // Error al actualizar
                        }
                    } else {
                        taskCompletionSource.setException(Exception("Intercambio no encontrado"))
                    }
                } else {
                    taskCompletionSource.setException(Exception("Documento no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }

    //Función para unirse a un intercambio
    fun agregarParticipante(docId: String, email: String, selectedTheme: String): Task<Boolean> {
        var personasRegistradasActualizadas = 0
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val intercambioRef = db.collection("intercambios").document(docId)

        intercambioRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val intercambio = document.toObject(Intercambio::class.java)
                    if (intercambio != null) {
                        // Buscar si el participante existe
                        val participanteExistente = intercambio.participantes.find { it.email == email }
                        val participantesActualizados = if (participanteExistente != null) {
                            intercambio.participantes.map { participante ->
                                if (participante.email == email) {
                                    if (participante.uid.isBlank()) {
                                        // Si el UID está vacío, lo asigna y activa
                                        participante.copy(uid = userId!!, activo = true, temaRegalo = selectedTheme)
                                    } else {
                                        // Si el UID no está vacío, solo activa al participante
                                        participante.copy(activo = true, temaRegalo = selectedTheme)
                                    }
                                } else {
                                    participante
                                }
                            }
                        } else {
                            // Si no encuentra el participante, lo agrega como nuevo
                            intercambio.participantes + Participante(
                                uid = userId!!,
                                email = email,
                                temaRegalo = selectedTheme,
                                asignadoA = "",
                                activo = true
                            )
                        }
                        // Actualizar personasRegistradas con los participantes activos
                        personasRegistradasActualizadas = participantesActualizados.size
                        // Actualizar en Firestore
                        intercambioRef.update(
                            mapOf(
                                "participantes" to participantesActualizados,
                                "personasRegistradas" to personasRegistradasActualizadas
                            )
                        ).addOnSuccessListener {
                            taskCompletionSource.setResult(true) // Actualización exitosa
                        }.addOnFailureListener { exception ->
                            taskCompletionSource.setException(exception) // Error al actualizar
                        }
                    } else {
                        taskCompletionSource.setException(Exception("Intercambio no encontrado"))
                    }
                } else {
                    taskCompletionSource.setException(Exception("Documento no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }



    //Función para editar el tema seleccionado
    fun editarTemaRegalo(docId: String, nuevoTema: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val intercambioRef = db.collection("intercambios").document(docId)

        intercambioRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val intercambio = document.toObject(Intercambio::class.java)
                    if (intercambio != null) {
                        val participantesActualizados = intercambio.participantes.map { participante ->
                            if (participante.uid == userId) {
                                participante.copy(temaRegalo = nuevoTema) // Actualiza el tema
                            } else {
                                participante
                            }
                        }

                        // Actualiza el intercambio con la lista modificada
                        intercambioRef.update("participantes", participantesActualizados)
                            .addOnSuccessListener {
                                taskCompletionSource.setResult(true) // Actualización exitosa
                            }.addOnFailureListener { exception ->
                                taskCompletionSource.setException(exception) // Error al actualizar
                            }
                    } else {
                        taskCompletionSource.setException(Exception("Intercambio no encontrado"))
                    }
                } else {
                    taskCompletionSource.setException(Exception("Documento no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                taskCompletionSource.setException(exception)
            }

        return taskCompletionSource.task
    }


    //Función para editar un intercambio
    suspend fun actualizarIntercambio(
        updated: Intercambio,
        docId: String
    ): Boolean = suspendCoroutine { continuation ->

        // Referencia a la subcolección de intercambios del usuario
        val intercambioRef = db.collection("intercambios").document(docId)

        // Guardar el objeto en Firestore
        intercambioRef.get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val intercambio = document.toObject(Intercambio::class.java)
                    if (intercambio != null) {
                        // Actualiza el intercambio con la lista modificada
                        intercambioRef.set(updated, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.e("EditarIntercambio", "Intercambio guardado exitosamente")
                                continuation.resume(true)  // Devuelve true si fue exitoso
                            }.addOnFailureListener {
                                Log.e("EditarIntercambio","Error al guardar el intercambio")
                                continuation.resume(false)  // Devuelve false si hubo un error
                            }
                    } else {
                        Log.e("EditarIntercambio","Error al guardar el intercambio")
                        continuation.resume(false)  // Devuelve false si hubo un error
                    }
                }else{
                    Log.e("EditarIntercambio","Error al guardar el intercambio")
                    continuation.resume(false)  // Devuelve false si hubo un error
                }

            }
            .addOnFailureListener { e ->
                Log.e("EditarIntercambio","Error al guardar el intercambio: ${e.message}")
                continuation.resume(false)  // Devuelve false si hubo un error
            }
    }


}