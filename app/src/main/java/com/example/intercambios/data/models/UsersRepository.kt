package com.example.intercambios.data.models

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersRepository {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    //Función para actualizar los datos de usuario
     fun actualizarUsuario(userData: Map<String, Any>, onResult:(Boolean) -> Unit) {
        if(userId != null){
            db.collection("users").document(userId).update(userData).addOnSuccessListener {
                onResult(true)
            }.addOnFailureListener {
                onResult(false)
            }
        }
        onResult(false)
    }

    //Una vez que se verifica se actualiza el estado en firebase
    fun updateVerifiedStatus() {
        if(userId != null){
            db.collection("users").document(userId)
                .update("verified", true) // Actualiza el campo en Firestore
                .addOnSuccessListener {
                    Log.d("EmailVerification", "Campo 'verified' actualizado en Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("EmailVerification", "Error al actualizar 'verified': ${e.message}")
                }
        }
    }

    //Actualizar el avatar
    fun updateAvatarImage(avatar: String) {
        if(userId != null){
            db.collection("users").document(userId)
                .update("avatar", avatar) // Actualiza el campo en Firestore
                .addOnSuccessListener {
                    Log.d("SelectAvatar", "Campo 'avatar' actualizado en Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("SelectAvatar", "Error al actualizar 'avatar': ${e.message}")
                }
        }
    }

    //Obtener usuario
    fun obtenerUsuario(callback: (Usuario?) -> Unit) {
        // Obtén el documento de la colección
        db.collection("users").document(userId!!).get()
            .addOnSuccessListener { document ->
                // Si el documento existe, lo convertimos en un objeto Usuario
                if (document != null && document.exists()) {
                    val usuario = document.toObject(Usuario::class.java)
                    callback(usuario)  // Llamamos al callback con el objeto Usuario
                } else {
                    callback(null)  // Si el documento no existe, pasamos null
                }
            }
            .addOnFailureListener { exception ->
                // Si hay un error al obtener el documento, lo manejamos aquí
                callback(null)
                exception.printStackTrace()
            }
    }

    fun obtenerUsuarioPorId(uid: String): Task<Usuario> {
        val taskCompletionSource = TaskCompletionSource<Usuario>()
        val consulta = db.collection("users").document(uid)

        consulta.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val usuario = document.toObject(Usuario::class.java)
                    usuario?.let {
                        taskCompletionSource.setResult(it)
                    } ?: run {
                        taskCompletionSource.setException(Exception("Usuario no encontrado"))
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



}