package com.example.intercambios.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.intercambios.R
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.intercambios.ui.HomeActivity
import com.example.intercambios.ui.ProviderType
import com.example.intercambios.ui.perfil.SelectAvatarActivity
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class GeneralUtils(private val context: Context){

     fun showAlert(message: String) {
         val inflater = LayoutInflater.from(context)
         val dialogView = inflater.inflate(R.layout.dialog_error_general, null)
         val errorText = dialogView.findViewById<TextView>(R.id.mensajeText)
         errorText.text = message

         val alertDialog = AlertDialog.Builder(context)
             .setView(dialogView)
             .create()

         dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
             alertDialog.dismiss()
         }

         alertDialog.show()
    }

    fun alertRecuperacion(message: String) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_error_general, null)
        val errorText = dialogView.findViewById<TextView>(R.id.mensajeText)
        val tituloError = dialogView.findViewById<TextView>(R.id.tituloError)
        tituloError.text = context.getString(R.string.recuperacion_pass)
        errorText.text = message

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }


     fun showHome(provider: ProviderType, email: String) {
        val homeIntent = Intent(context, HomeActivity::class.java)
         val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE)
         prefs.edit().putString("provider", provider.toString()).putString("email", email).apply()
         // Si el contexto es una actividad, finalizarla
         if (context is Activity) {
             context.finish() // Finaliza la actividad actual
         }
        context.startActivity(homeIntent)//regresa a la pantalla principal
    }

    //Función para primera vez :)
    fun showAvatars(provider: ProviderType, email: String) {
        val avatarIntent = Intent(context, SelectAvatarActivity::class.java)
        val prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.prefs_file), Context.MODE_PRIVATE)
        prefs.edit().putString("provider", provider.toString()).putString("email", email).apply()
        // Si el contexto es una actividad, finalizarla
        if (context is Activity) {
            context.finish() // Finaliza la actividad actual
        }
        context.startActivity(avatarIntent)//regresa a la pantalla principal
    }

    fun generarCodigoUnicoConHash(): String {
        // Obtener el timestamp
        val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss", Locale.US)
        val timestamp = LocalDateTime.now().format(formatter)

        // Crear un hash del timestamp
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(timestamp.toByteArray())
            .joinToString("") { "%02x".format(it) } // Convertir a hexadecimal

        val randomPart = UUID.randomUUID().toString().substring(0, 3) // 3 caracteres aleatorios

        // Tomar los primeros 8 caracteres del hash
        return  "${hash.substring(0, 5).uppercase()}${randomPart.uppercase()}"// Convertir a mayúsculas
    }

}