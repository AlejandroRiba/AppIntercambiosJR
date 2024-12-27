package com.example.intercambios.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.example.intercambios.R
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
        validarCampoTexto(binding.edTextNombre, binding.textInputLayoutNombreIntercambio, context.getString(R.string.campo_obligatorio))
        validarCampoTexto(binding.edTextMontoMax, binding.textInputLayoutMonto, context.getString(R.string.campo_obligatorio))
        validarCampoNumerico(binding.edTextNumPersonas, binding.textInputLayoutPersonas)
        validarCampoTexto(binding.edTextHora, binding.textInputLayoutHoraIntercambio, context.getString(R.string.campo_obligatorio))
        validarCampoTexto(binding.edTextLugar, binding.textInputLayoutLugarIntercambio, context.getString(R.string.campo_obligatorio))
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

        val input = editText.text
        if (input.isNotBlank()) {
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
            isFormValid = true
        }
    }

    private fun validarCampoNumerico(editText: EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isBlank()) {
                    textInputLayout.error = context.getString(R.string.campo_obligatorio)
                    isFormValid = false
                } else {
                    val number = input.toIntOrNull()
                    if (number == null || number < 2) {
                        textInputLayout.error = context.getString(R.string.num_personas_mayor_uno)
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

        val input = editText.text.toString()
        if (input.isNotBlank()) {
            val number = input.toIntOrNull()
            if (number == null || number < 2) {
                textInputLayout.error = context.getString(R.string.num_personas_mayor_uno)
                isFormValid = false
            } else {
                textInputLayout.error = null
                textInputLayout.isErrorEnabled = false
                isFormValid = true
            }
        }

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
                    fechaRegistroLayout.error = context.getString(R.string.campo_obligatorio)
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
                                binding.textInputLayoutFechaIntercambio.error = context.getString(R.string.ingresar_fecha_interc)
                                isFormValid = false
                            } else { //si el campo si tiene un valor comprobamos
                                // Si la fecha de registro es posterior a la fecha del intercambio, mostramos el error
                                // Calcula la diferencia en milisegundos
                                val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                                // Convierte la diferencia a días
                                val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                                if (fechaRegistro.after(fechaIntercambio)) {
                                    fechaRegistroLayout.error = context.getString(R.string.fecha_posterior_error)
                                    isFormValid = false
                                }else if(diferenciaEnDias < 1){
                                    fechaRegistroLayout.error = context.getString(R.string.dia_holgura)
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
                        fechaRegistroLayout.error = context.getString(R.string.formato_fecha_invalido)
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val input = fechaRegistroEdtxt.text.toString()
        if (input.isNotBlank()) {
            try {
                val fechaRegistro = sdf.parse(input)
                if(fechaRegistro != null){
                    val fechaIntercambioString = fechaIntercambioEdtxt.text.toString()
                    val fechaIntercambio = if (fechaIntercambioString.isNotEmpty()) {
                        sdf.parse(fechaIntercambioString)
                    } else null

                    if (fechaIntercambio == null) { //el campo de fecha de intercambio sigue vacio, entonces lo indicamos
                        // Si no se ha ingresado la fecha de intercambio, mostramos el error
                        binding.textInputLayoutFechaIntercambio.error = context.getString(R.string.ingresar_fecha_interc)
                        isFormValid = false
                    } else { //si el campo si tiene un valor comprobamos
                        // Si la fecha de registro es posterior a la fecha del intercambio, mostramos el error
                        // Calcula la diferencia en milisegundos
                        val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                        // Convierte la diferencia a días
                        val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                        if (fechaRegistro.after(fechaIntercambio)) {
                            fechaRegistroLayout.error = context.getString(R.string.fecha_posterior_error)
                            isFormValid = false
                        }else if(diferenciaEnDias < 1){
                            fechaRegistroLayout.error = context.getString(R.string.dia_holgura)
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
                fechaRegistroLayout.error = context.getString(R.string.formato_fecha_invalido)
                isFormValid = false
            }
        }

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
                    fechaIntercambioLayout.error =  context.getString(R.string.campo_obligatorio)
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
                                fechaRegistroLayout.error = context.getString(R.string.ingresar_fecha_registro)
                                isFormValid = false
                            } else {
                                // Si la fecha de registro es posterior a la fecha de intercambio, mostramos el error
                                // Calcula la diferencia en milisegundos
                                val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                                // Convierte la diferencia a días
                                val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                                if (fechaRegistro.after(fechaIntercambio)) {
                                    fechaIntercambioLayout.error = context.getString(R.string.fecha_posterior_error)
                                    isFormValid = false
                                } else if(diferenciaEnDias < 1){
                                    fechaIntercambioLayout.error = context.getString(R.string.dia_holgura)
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
                        fechaIntercambioLayout.error =  context.getString(R.string.formato_fecha_invalido)
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val input = fechaIntercambioEdtxt.text.toString()
        if (input.isNotBlank()) {
            try {
                val fechaIntercambio = sdf.parse(input)
                if(fechaIntercambio != null){
                    val fechaRegistroString = fechaRegistroEdtxt.text.toString()
                    val fechaRegistro = if (fechaRegistroString.isNotEmpty()) {
                        sdf.parse(fechaRegistroString)
                    } else null

                    if (fechaRegistro == null) {
                        // Si no se ha ingresado la fecha de registro, mostramos el error
                        fechaRegistroLayout.error =  context.getString(R.string.ingresar_fecha_registro)
                        isFormValid = false
                    } else {
                        // Si la fecha de registro es posterior a la fecha de intercambio, mostramos el error
                        // Calcula la diferencia en milisegundos
                        val diferenciaEnMilisegundos = fechaIntercambio.time - fechaRegistro.time
                        // Convierte la diferencia a días
                        val diferenciaEnDias = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24)
                        if (fechaRegistro.after(fechaIntercambio)) {
                            fechaIntercambioLayout.error =  context.getString(R.string.fecha_posterior_error)
                            isFormValid = false
                        } else if(diferenciaEnDias < 1){
                            fechaIntercambioLayout.error =  context.getString(R.string.dia_holgura)
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
                fechaIntercambioLayout.error =  context.getString(R.string.formato_fecha_invalido)
                isFormValid = false
            }
        }

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
