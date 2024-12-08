package com.example.intercambios.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.intercambios.R
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AuthUtils(private val context: Context){

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // Inicio de sesión con correo y contraseña
    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user != null) {
                // Verificar si el displayName es nulo o vacío
                if (user.displayName.isNullOrEmpty()) {
                    // Si displayName es nulo, obtener el nombre desde Firestore
                    val userName = getUserNameFromFirestore(user.uid)

                    // Si se obtiene el nombre desde Firestore, actualizar el displayName
                    if (userName != null) {
                        updateDisplayName(user, userName)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al iniciar sesión con email: ${e.message}")
            false
        }
    }

     suspend fun getUserNameFromFirestore(userId: String): String? {
        return try {
            // Obtener el nombre del usuario desde Firestore
            val userDocument = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
            userDocument.getString("name")
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al obtener el nombre de Firestore: ${e.message}")
            null
        }
    }

    // Registro de usuario con correo y contraseña
    suspend fun registerWithEmail(email: String, password: String, name: String, alias: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return false

            // Guardar datos del perfil en Firestore
            val userData = mapOf(
                "nombre" to name,
                "alias" to alias,
                "email" to email,
                "verified" to false,
                "avatar" to "avatardef"
            )
            firestore.collection("users").document(userId).set(userData).await()
            val user = auth.currentUser
            if (user != null) {
                updateDisplayName(user, name)
            }
            // Enviar correo de verificación
            val emailSent = sendEmailVerification()
            if (emailSent) {
                Log.e("FirebaseHelper", "Correo de Verificación enviado")
            }
            emailSent
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al registrar usuario: ${e.message}")
            false
        }
    }

    //Envia correo de verificacion para usuarios que se registran con Email & Contrasena
    private suspend fun sendEmailVerification(): Boolean {
        val user = auth.currentUser
        return try {
            user?.sendEmailVerification()?.await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al enviar correo de verificación: ${e.message}")
            false
        }
    }

    //Reenvia el email en caso de que no llegue
     fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("EmailVerification", "Correo de verificación reenviado")
            } else {
                Log.e("EmailVerification", "Error al reenviar correo: ${task.exception?.message}")
            }
        }
    }


    // Inicio de sesión con Google
    suspend fun loginWithGoogle(): Pair<Boolean, Boolean> {
        return try {
            val result = buildCredentialRequest()
            var firstTime = false
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("LoginActivity", "ID Token recibido: ${googleIdTokenCredential.idToken}")
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val data = Firebase.auth.signInWithCredential(firebaseCredential).await()
                Log.d("LoginActivity", "Inicio de sesión con Google exitoso para usuario: ${data.user?.displayName}")
                // Guardar datos del perfil en Firestore
                // Verificar si el usuario ya existe en Firestore
                val userDoc = firestore.collection("users").document(data.user?.uid.toString()).get().await()
                if (!userDoc.exists()) {
                    // Si no existe, guardar los datos del perfil en Firestore
                    firstTime = true
                    val userData = mapOf(
                        "nombre" to data.user?.displayName,
                        "alias" to data.user?.displayName, // Aquí puedes agregar lógica adicional para generar un alias único si es necesario
                        "email" to data.user?.email,
                        "verified" to true,
                        "avatar" to "avatardef"
                    )
                    firestore.collection("users").document(data.user?.uid.toString()).set(userData).await()
                }
                Pair(true, firstTime)
            } else {
                Log.e("LoginActivity", "Credencial no válida recibida.")
                Pair(false, false)
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al iniciar sesión con Google: ${e.message}")
            Pair(false, false)
        }
    }

     private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()

        return credentialManager.getCredential(
            request = request, context = context
        )
    }

    private fun updateDisplayName(user: FirebaseUser, displayName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d("Auth", "El displayName se actualizó correctamente.")
                } else {
                    Log.e("Auth", "Error al actualizar el displayName.")
                }
            }
    }


    // Cerrar sesión
    fun logout() {
        val prefs = context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        auth.signOut()
    }

    // Obtener el usuario actual
    fun getCurrentUser() = auth.currentUser


}