package com.example.intercambios.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.google.android.material.textfield.TextInputLayout
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class FormularioValidator(
    private var binding: NuevoIntercambioBinding,
    private var context: Context
) {

    var isFormValid = false

    fun validarFormulario() {
        validarFechaRegistro(binding.edTextFechaRegistro, binding.textInputLayoutFechaRegistro, binding.edTextFechaIntercambio, binding.textInputLayoutFechaIntercambio)
        validarFechaIntercambio(binding.edTextFechaIntercambio, binding.textInputLayoutFechaIntercambio, binding.edTextFechaRegistro, binding.textInputLayoutFechaRegistro)
        validarCampoTexto(binding.edTextNombre, binding.textInputLayoutNombreIntercambio, "Este campo es obligatorio")
        validarCampoTexto(binding.edTextMontoMax, binding.textInputLayoutMonto, "Este campo es obligatorio")
        validarCampoNumerico(binding.edTextNumPersonas, binding.textInputLayoutPersonas)
        validarCampoTexto(binding.edTextHora, binding.textInputLayoutHoraIntercambio, "Este campo es obligatorio")
        validarCampoTexto(binding.edTextLugar, binding.textInputLayoutLugarIntercambio, "Este campo es obligatorio")
    }

    private fun validarCampoTexto(editText: EditText, textInputLayout: TextInputLayout, errorMsg: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isBlank()) {
                    textInputLayout.error = errorMsg
                    isFormValid = false
                } else {
                    textInputLayout.error = null
                    textInputLayout.isErrorEnabled = false
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarCampoNumerico(editText: EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isBlank()) {
                    textInputLayout.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    val number = input.toIntOrNull()
                    if (number == null || number < 2) {
                        textInputLayout.error = "El número de personas debe ser mayor a uno"
                        isFormValid = false
                    } else {
                        textInputLayout.error = null
                        textInputLayout.isErrorEnabled = false
                        isFormValid = true
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarFechaRegistro(
        fechaRegistroEdtxt: EditText,
        fechaRegistroLayout: TextInputLayout,
        fechaIntercambioEdtxt: EditText,
        fechaIntercambioLayout: TextInputLayout
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fechaRegistroEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isBlank()) {
                    fechaRegistroLayout.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    try {
                        val fechaRegistro = sdf.parse(input)
                        if(fechaRegistro != null){
                            val fechaIntercambioString = fechaIntercambioEdtxt.text.toString()
                            val fechaIntercambio = if (fechaIntercambioString.isNotEmpty()) {
                                sdf.parse(fechaIntercambioString)
                            } else null

                            if (fechaIntercambio == null) { //el campo de fecha de intercambio sigue vacio, entonces lo indicamos
                                // Si no se ha ingresado la fecha de intercambio, mostramos el error
                                binding.textInputLayoutFechaIntercambio.error = "Debe ingresar una fecha de intercambio"
                                isFormValid = false
                            } else { //si el campo si tiene un valor comprobamos
                                // Si la fecha de registro es posterior a la fecha del intercambio, mostramos el error
                                // Calcula la diferencia en milisegundos
                                val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                                // Convierte la diferencia a días
                                val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                                if (fechaRegistro.after(fechaIntercambio)) {
                                    fechaRegistroLayout.error = "La fecha de registro no puede ser posterior a la fecha del intercambio"
                                    isFormValid = false
                                }else if(diferenciaEnDias < 1){
                                    fechaRegistroLayout.error = "Debe haber al menos un día de diferencia entre las fechas"
                                    isFormValid = false
                                } else{
                                    // Si todo es correcto, eliminamos el error en ambos inputs
                                    fechaRegistroLayout.error = null
                                    fechaIntercambioLayout.error = null
                                    fechaRegistroLayout.isErrorEnabled = false
                                    fechaIntercambioLayout.isErrorEnabled = false
                                    isFormValid = true
                                }
                            }
                        }
                    } catch (e: ParseException) {
                        fechaRegistroLayout.error = "Formato de fecha inválido"
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarFechaIntercambio(
        fechaIntercambioEdtxt: EditText,
        fechaIntercambioLayout: TextInputLayout,
        fechaRegistroEdtxt: EditText,
        fechaRegistroLayout: TextInputLayout
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fechaIntercambioEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isBlank()) {
                    fechaIntercambioLayout.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    try {
                        val fechaIntercambio = sdf.parse(input)
                        if(fechaIntercambio != null){
                            val fechaRegistroString = fechaRegistroEdtxt.text.toString()
                            val fechaRegistro = if (fechaRegistroString.isNotEmpty()) {
                                sdf.parse(fechaRegistroString)
                            } else null

                            if (fechaRegistro == null) {
                                // Si no se ha ingresado la fecha de registro, mostramos el error
                                fechaRegistroLayout.error = "Debe ingresar una fecha de registro"
                                isFormValid = false
                            } else {
                                // Si la fecha de registro es posterior a la fecha de intercambio, mostramos el error
                                // Calcula la diferencia en milisegundos
                                val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                                // Convierte la diferencia a días
                                val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                                if (fechaRegistro.after(fechaIntercambio)) {
                                    fechaIntercambioLayout.error = "La fecha de registro no puede ser posterior a la fecha del intercambio"
                                    isFormValid = false
                                } else if(diferenciaEnDias < 1){
                                    fechaIntercambioLayout.error = "Debe haber al menos un día de diferencia entre las fechas"
                                    isFormValid = false
                                } else {
                                    // Si todo es correcto, eliminamos el error en ambos inputs
                                    fechaIntercambioLayout.error = null
                                    fechaRegistroLayout.error = null
                                    fechaRegistroLayout.isErrorEnabled = false
                                    fechaIntercambioLayout.isErrorEnabled = false
                                    isFormValid = true
                                }
                            }

                        }

                    } catch (e: ParseException) {
                        fechaIntercambioLayout.error = "Formato de fecha inválido"
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun getValid() :Boolean{
        val nombre = binding.edTextNombre.text.toString()
        val personas = binding.edTextNumPersonas.text.toString()
        val montoMax = binding.edTextMontoMax.text.toString()
        val fechaIntercambio = binding.edTextFechaIntercambio.text.toString()
        val fechaRegistro = binding.edTextFechaRegistro.text.toString()
        val horaIntercambio = binding.edTextHora.text.toString()
        val lugarIntercambio = binding.edTextLugar.text.toString()
        return nombre.isNotEmpty() && personas.isNotEmpty() && montoMax.isNotEmpty() && fechaIntercambio.isNotEmpty() && fechaRegistro.isNotEmpty() && horaIntercambio.isNotEmpty() && lugarIntercambio.isNotEmpty() && isFormValid
    }
}
