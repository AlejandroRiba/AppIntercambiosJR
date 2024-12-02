package com.example.intercambios.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.intercambios.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

class AuthUtils(private val context: Context){

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val credentialManager = CredentialManager.create(context)

    // Inicio de sesión con correo y contraseña
    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al iniciar sesión con email: ${e.message}")
            false
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
                "verified" to false
            )
            firestore.collection("users").document(userId).set(userData).await()
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
    suspend fun loginWithGoogle(): Boolean {
        return try {
            val result = buildCredentialRequest()
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
                    val userData = mapOf(
                        "nombre" to data.user?.displayName,
                        "alias" to data.user?.displayName, // Aquí puedes agregar lógica adicional para generar un alias único si es necesario
                        "email" to data.user?.email,
                        "verified" to true
                    )
                    firestore.collection("users").document(data.user?.uid.toString()).set(userData).await()
                }
                true
            } else {
                Log.e("LoginActivity", "Credencial no válida recibida.")
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error al iniciar sesión con Google: ${e.message}")
            false
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