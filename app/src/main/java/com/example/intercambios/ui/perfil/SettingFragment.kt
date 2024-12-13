package com.example.intercambios.ui.perfil

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.intercambios.R
import com.example.intercambios.R.*
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.*

class SettingFragment : Fragment(layout.fragment_settings) {

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languageSwitch = view.findViewById<MaterialSwitch>(R.id.language_switch)

        // Configura el comportamiento del interruptor de idioma
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Cambiar a inglés
                setLocale("en")
                languageSwitch.text = getString(R.id.language_switch) // Actualizar el texto dinámicamente
                Toast.makeText(requireContext(), "Language changed to English", Toast.LENGTH_SHORT).show()
            } else {
                // Cambiar a español
                setLocale("es")
                languageSwitch.text = getString(R.id.language_switch) // Actualizar el texto dinámicamente
                Toast.makeText(requireContext(), "Idioma cambiado a Español", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)

        // Reinicia el fragmento para que las cadenas cambien dinámicamente
        requireActivity().supportFragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }
}