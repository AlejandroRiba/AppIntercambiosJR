package com.example.intercambios.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.intercambios.BaseActivity
import com.example.intercambios.R
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.ui.ProviderType
import com.example.intercambios.utils.GeneralUtils
import com.example.intercambios.utils.SortManager.cancelarAlarmaSorteo
import com.example.intercambios.utils.SortManager.configurarAlarmaSorteo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private val firebaseHelper = AuthUtils(this)
    private val genUtils = GeneralUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun showNoConnectionScreen() {
        setContentView(R.layout.loading_layout)
        aplicarWindowInsets()
    }

    override fun hideNoConnectionScreen() {
        setContentView(R.layout.register_layout)
        aplicarWindowInsets()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.paintFlags = btnLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        btnLogin.setOnClickListener {
            // Cambia al login
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)//regresa a la pantalla principal
            finish()//finaliza la actividad
        }

        //Setup
        setup()
    }

    private fun setup() {
        val nombre = findViewById<EditText>(R.id.etNombre)
        val alias = findViewById<EditText>(R.id.etAlias)
        val correo = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            val loadingDialog = mostrarCarga()
            if (nombre.text.isNotEmpty() && alias.text.isNotEmpty() && correo.text.isNotEmpty() && password.text.isNotEmpty()) {
                lifecycleScope.launch {
                    val success = firebaseHelper.registerWithEmail(
                        correo.text.toString(),
                        password.text.toString(),
                        nombre.text.toString(),
                        alias.text.toString()
                    )
                    loadingDialog.dismiss()
                    if (success) {
                        val intent = Intent(this@RegisterActivity, EmailVerification::class.java)
                        finish()
                        startActivity(intent)
                    } else {
                        genUtils.showAlert(getString(R.string.email_existe))
                    }
                }
            }else{
                genUtils.showAlert(getString(R.string.envio_denegado_campos_vacios))
                loadingDialog.dismiss()
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
                                reprogramarSorteos()
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

    }

    private fun mostrarCarga(): AlertDialog {
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

    private fun reprogramarSorteos(){
        try {
            val userId = Firebase.auth.currentUser?.uid
            if(userId != null){
                val intercambioUtils = IntercambioRepository()
                intercambioUtils.obtenerIntercambios().addOnSuccessListener { intercambios ->
                    if(intercambios.isNotEmpty()){
                        for ((intercambio, documentId) in intercambios) {
                            if(!intercambio.sorteo && intercambio.organizador == userId){
                                configurarAlarmaSorteo(this, intercambio.fechaMaxRegistro,documentId)
                            }
                        }
                    }
                }
                Log.i("Login", "Reprogramar alarmas")
            }
        } catch (e: Exception) {
            Log.e("Logout", "Error en el proceso de logout: ${e.message}")
        }
    }

}