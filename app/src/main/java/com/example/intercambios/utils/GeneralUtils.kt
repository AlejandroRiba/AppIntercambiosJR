package com.example.intercambios.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.intercambios.R
import android.content.Intent
import android.content.SharedPreferences
import com.example.intercambios.ui.HomeActivity
import com.example.intercambios.ui.ProviderType

class GeneralUtils(private val context: Context){

     fun showAlert(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("ERROR")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
}