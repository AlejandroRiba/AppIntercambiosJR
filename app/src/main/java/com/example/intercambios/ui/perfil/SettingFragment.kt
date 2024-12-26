package com.example.intercambios.ui.perfil

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.intercambios.R
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.*

class SettingFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // Obtén referencias a los switches y labels
        val languageSwitch = view.findViewById<MaterialSwitch>(R.id.switchIdioma)
        val themeSwitch = view.findViewById<MaterialSwitch>(R.id.switchTema)
        val languageLabel = view.findViewById<TextView>(R.id.labelIdioma)
        val themeLabel = view.findViewById<TextView>(R.id.labelTema)

        // Configura el estado inicial desde las preferencias
        val currentLanguage = sharedPreferences.getString("language", "es") ?: "es"
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        // Actualiza el estado inicial del interruptor de idioma
        languageSwitch.isChecked = currentLanguage == "en"
        languageLabel.text = if (currentLanguage == "en") getString(R.string.ingles) else getString(R.string.espanol)

        // Configura el comportamiento del interruptor de idioma
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newLanguage = if (isChecked) "en" else "es"
            setLocale(newLanguage)
            languageLabel.text = if (isChecked) getString(R.string.ingles) else getString(R.string.espanol)
            Toast.makeText(
                requireContext(),
                getString(R.string.language_changed, languageLabel.text),
                Toast.LENGTH_SHORT
            ).show()

            // Guarda la preferencia
            sharedPreferences.edit().putString("language", newLanguage).apply()
        }

        // Actualiza el estado inicial del interruptor de tema
        themeSwitch.isChecked = isDarkMode
        themeLabel.text = if (isDarkMode) getString(R.string.oscuro) else getString(R.string.claro)

        // Configura el comportamiento del interruptor de tema
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(newMode)
            themeLabel.text = if (isChecked) getString(R.string.oscuro) else getString(R.string.claro)
            Toast.makeText(
                requireContext(),
                getString(R.string.theme_changed, themeLabel.text),
                Toast.LENGTH_SHORT
            ).show()

            // Guarda la preferencia
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)

        // Reinicia el fragmento para reflejar los cambios
        requireActivity().supportFragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }
}
