package com.example.intercambios.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import android.app.AlertDialog
import android.text.InputType
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.intercambios.BaseActivity
import com.example.intercambios.R
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.ui.ProviderType
import com.example.intercambios.ui.perfil.SelectAvatarActivity
import com.example.intercambios.utils.GeneralUtils
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private val firebaseHelper = AuthUtils(this)
    private val genUtils = GeneralUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun hideNoConnectionScreen() {
        setContentView(R.layout.login_layout)
        aplicarWindowInsets()

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            // Cambia al registro
            val regIntent = Intent(this, RegisterActivity::class.java)
            finish()//finaliza la actividad
            startActivity(regIntent)//regresa a la pantalla principal
        }

        //SETUP
        setup()
    }

    override fun showNoConnectionScreen() {
        setContentView(R.layout.loading_layout)
        aplicarWindowInsets()
    }

    private fun setup() {
        val correo = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val animationView = findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.animateSending)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val loadingDialog = mostrarCarga()
            if (correo.text.isNotEmpty() && password.text.isNotEmpty()) {
                lifecycleScope.launch {
                    val success = firebaseHelper.loginWithEmail(correo.text.toString(), password.text.toString())
                    // Cuando la tarea haya terminado, cerrar el diálogo
                    loadingDialog.dismiss()
                    aplicarWindowInsets()
                    if (success) {
                        val user = firebaseHelper.getCurrentUser()
                        if (user != null) {
                            genUtils.showHome(ProviderType.BASIC, user.email.toString())
                        }
                    } else {
                        genUtils.showAlert(getString(R.string.credenciales_erroneas))
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnGoogle).setOnClickListener {
            val loadingDialog = mostrarCarga()
            lifecycleScope.launch {
                try {
                    val success = firebaseHelper.loginWithGoogle()
                    // Cuando la tarea haya terminado, cerrar el diálogo
                    loadingDialog.dismiss()
                    aplicarWindowInsets()
                    if(success.first){
                        val user = firebaseHelper.getCurrentUser()
                        if (user != null) {
                            if(success.second){
                                genUtils.showAvatars(ProviderType.GOOGLE, user.email.toString())
                            }else{
                                genUtils.showHome(ProviderType.GOOGLE, user.email.toString())
                            }
                        }
                    }
                } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                    Log.e("LoginActivity", "Inicio de sesión cancelado por el usuario.")
                    // Cuando la tarea haya terminado, cerrar el diálogo
                    loadingDialog.dismiss()
                    aplicarWindowInsets()
                    //genUtils.showAlert("Inicio de sesión cancelado por el usuario.")
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error durante el inicio de sesión con Google: ${e.message}", e)
                    // Cuando la tarea haya terminado, cerrar el diálogo
                    loadingDialog.dismiss()
                    aplicarWindowInsets()
                    genUtils.showAlert(getString(R.string.error_inicio_google, e.message))
                }
            }
        }

        findViewById<Button>(R.id.btnForgotPassword).setOnClickListener {
            val email = correo.text.toString()
            if (email.isNotEmpty()) {
                // Muestra la animación mientras se envía el correo
                animationView.visibility = View.VISIBLE

                firebaseHelper.sendPasswordResetEmail(email) { success ->
                    animationView.visibility = View.GONE // Oculta la animación
                    if (success) {
                        genUtils.alertRecuperacion(getString(R.string.recuperacion_enviado))
                    } else {
                        genUtils.alertRecuperacion(getString(R.string.recuperacion_error_enviado))
                    }
                }
            } else {
                genUtils.showAlert(getString(R.string.correo_requerido))
            }
        }


    }

    private fun mostrarCarga(): AlertDialog{
        // Inflar el layout personalizado con Lottie y el texto
        val dialogView = LayoutInflater.from(this).inflate(R.layout.loading_layout, null)

        // Crear el diálogo con el layout inflado
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // No permitirá cerrar tocando fuera del diálogo
            .create()

        // Mostrar el diálogo
        builder.show()

        return builder
    }

    private fun aplicarWindowInsets() {
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainView.requestApplyInsets()
    }

}