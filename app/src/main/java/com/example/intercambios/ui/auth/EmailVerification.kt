package com.example.intercambios.ui.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.intercambios.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.data.models.Users
import com.example.intercambios.ui.ProviderType
import com.example.intercambios.utils.GeneralUtils
import com.google.firebase.auth.FirebaseAuth

class EmailVerification : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private val firebaseHandler = AuthUtils(this)
    private val resendDelay = 60000L // 1 minuto en milisegundos
    private lateinit var resendButton: Button
    private val genUtils = GeneralUtils(this)
    private val usersHelper = Users()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.email_verification_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resendButton = findViewById(R.id.btnResendEmail)
        resendButton.setOnClickListener {
            firebaseHandler.resendVerificationEmail()
        }

        auth = FirebaseAuth.getInstance()

        checkEmailVerification()
        startResendCountdown()
    }

    private fun checkEmailVerification() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                auth.currentUser?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                            Log.d("EmailVerification", "Correo verificado")
                            usersHelper.updateVerifiedStatus() // Actualiza en Firestore
                            genUtils.showAvatars(ProviderType.BASIC, auth.currentUser?.email.toString())
                        } else {
                            Log.d("EmailVerification", "Correo aún no verificado")
                            handler.postDelayed(this, 3000) // Revisa cada 3 segundos
                        }
                    } else {
                        Log.e("EmailVerification", "Error al recargar usuario: ${task.exception?.message}")
                        genUtils.showAlert("Ocurrió un problema verificando tu cuenta. Inténtalo más tarde.")
                    }
                }
            }
        }, 3000) // Espera 3 segundos antes de la primera verificación
    }

    // Llama a esta función después de enviar un correo
    private fun startResendCountdown() {
        resendButton.visibility = View.GONE // Oculta el botón inicialmente
        handler.postDelayed({
            resendButton.visibility = View.VISIBLE // Muestra el botón después del retraso
        }, resendDelay)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Detén el handler al destruir la actividad
    }

}