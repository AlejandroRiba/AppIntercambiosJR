package com.example.intercambios.ui.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.example.intercambios.utils.GeneralUtils
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

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            // Cambia al login
            val loginIntent = Intent(this, LoginActivity::class.java)
            finish()//finaliza la actividad
            startActivity(loginIntent)//regresa a la pantalla principal
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
                    val success = firebaseHelper.registerWithEmail(correo.text.toString(), password.text.toString(), nombre.text.toString(), alias.text.toString())
                    // Cuando la tarea haya terminado, cerrar el diálogo
                    loadingDialog.dismiss()
                    aplicarWindowInsets()
                    if (success) {
                        // Si el registro fue exitoso, redirige a la pantalla de verificación
                        val intent = Intent(this@RegisterActivity, EmailVerification::class.java)
                        finish()  // Termina la actividad actual
                        startActivity(intent)
                    } else {
                        // Si hubo un error, muestra un mensaje adecuado al usuario
                        genUtils.showAlert("Error al registrar usuario")
                    }
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

}