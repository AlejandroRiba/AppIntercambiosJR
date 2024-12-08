package com.example.intercambios.ui.intercambio

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.intercambios.R
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.example.intercambios.utils.ColorSpinnerAdapter
import com.example.intercambios.utils.DatePickerFragment
import com.example.intercambios.utils.FormularioValidator
import com.example.intercambios.utils.GeneralUtils
import com.example.intercambios.utils.TimePickerFragment
import com.google.firebase.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class CrearIntercambioFragment : Fragment() {
    private var _binding: NuevoIntercambioBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var spinnerColor: Spinner
    private lateinit var nombreEdtxt: EditText
    private lateinit var personasEdtxt: EditText
    private lateinit var descripcionEdtxt: EditText
    private lateinit var montoMaxEdtxt: EditText
    private lateinit var fechaRegistroEdtxt: EditText
    private lateinit var fechaIntercambioEdtxt: EditText
    private lateinit var horaIntercambioEdtxt: EditText
    private lateinit var lugarIntercambioEdtxt: EditText

    private lateinit var selectedcolor: String
    private lateinit var nombre: String
    private lateinit var personas: String
    private lateinit var descripcion: String
    private lateinit var montoMax: String
    private lateinit var fechaRegistro: String
    private lateinit var fechaIntercambio: String
    private lateinit var horaIntercambio: String
    private lateinit var lugarIntercambio: String
    private lateinit var botonGuardar: Button

    private var isFormValid by Delegates.notNull<Boolean>()

    private lateinit var genUtils: GeneralUtils

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = NuevoIntercambioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genUtils = GeneralUtils(requireActivity())

        nombreEdtxt = binding.edTextNombre
        personasEdtxt = binding.edTextNumPersonas
        descripcionEdtxt = binding.exchangeDescriptionEditText
        montoMaxEdtxt = binding.edTextMontoMax
        fechaRegistroEdtxt = binding.edTextFechaRegistro
        fechaIntercambioEdtxt = binding.edTextFechaIntercambio
        horaIntercambioEdtxt = binding.edTextHora
        lugarIntercambioEdtxt = binding.edTextLugar
        botonGuardar = binding.createExchangeButton
        spinnerColor = binding.colorSpinner

        //asigna las opciones al spinner
        inicializaSpinners()

        horaIntercambioEdtxt.setOnClickListener { showTimePickerDialog(horaIntercambioEdtxt) }
        fechaIntercambioEdtxt.setOnClickListener{ showDatePickerDialog(fechaIntercambioEdtxt) }
        fechaRegistroEdtxt.setOnClickListener{ showDatePickerDialog(fechaRegistroEdtxt)  }

        isFormValid = false  // Flag para verificar si el formulario es válido
        //Mandar a crear los verificadores
        val validator = FormularioValidator(binding, requireActivity())
        validator.validarFormulario()

        botonGuardar.setOnClickListener {
            if(validator.getValid()) {
                sendFeedBack()
            }else{
                genUtils.showAlert("No se pudo enviar el formulario. Verifica de nuevo.")
            }
        }

    }

    private fun sendFeedBack(){
        nombre = nombreEdtxt.text.toString()
        personas = personasEdtxt.text.toString()
        montoMax = montoMaxEdtxt.text.toString()
        fechaIntercambio = fechaIntercambioEdtxt.text.toString()
        fechaRegistro = fechaRegistroEdtxt.text.toString()
        descripcion = descripcionEdtxt.text.toString()
        horaIntercambio = horaIntercambioEdtxt.text.toString()
        lugarIntercambio = lugarIntercambioEdtxt.text.toString()
        if(nombre.isNotEmpty() && personas.isNotEmpty() && montoMax.isNotEmpty() && fechaIntercambio.isNotEmpty() && fechaRegistro.isNotEmpty() && horaIntercambio.isNotEmpty() && lugarIntercambio.isNotEmpty()) {
            val unicode = genUtils.generarCodigoUnicoConHash()
            if(descripcion.isEmpty())
                descripcion = getString(R.string.no_descripcion)
            val newIntercambio = mapOf(
                "code" to unicode,
                "nombre" to nombre,
                "numPersonas" to personas,
                "descripcion" to descripcion,
                "fechaMaxRegistro" to fechaRegistro,
                "fechaIntercambio" to fechaIntercambio,
                "horaIntercambio" to horaIntercambio,
                "lugarIntercambio" to lugarIntercambio,
                "color" to selectedcolor,
                "personasRegistradas" to "1", //solo se registra la persona que lo crea
            )
            Log.i("CrearIntercambio", newIntercambio.toString())
        }else{
            Toast.makeText(requireActivity(), "Es necesario rellenar todos los campos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun validarFormulario(){
        //Verificación del nombre
        nombreEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutNombreIntercambio.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    binding.textInputLayoutNombreIntercambio.error = null
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación del nombre
        nombreEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutNombreIntercambio.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    binding.textInputLayoutNombreIntercambio.error = null
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación del monto máximo
        montoMaxEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutMonto.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    binding.textInputLayoutMonto.error = null
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación para el numero de personas
        personasEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutPersonas.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    // Intenta convertir el texto a un número
                    val number = input.toIntOrNull()

                    // Si es nulo o no es un número par, muestra un error
                    if (number == null || number % 2 != 0) {
                        binding.textInputLayoutPersonas.error = "El número de personas debe ser un número par"
                        isFormValid = false
                    } else {
                        binding.textInputLayoutPersonas.error = null
                        isFormValid = true
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // Validación para la fecha de registro
        fechaRegistroEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Validación de campo vacío
                if (input.isBlank()) {
                    binding.textInputLayoutFechaRegistro.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    try {
                        // Parseamos la fecha de registro (fecha ingresada)
                        val fechaRegistro = sdf.parse(input)

                        // Verificamos que la fecha de registro sea válida
                        if (fechaRegistro != null) {
                            // Validación de la fecha de intercambio, obtenemos la fecha de intercambio cada vez que cambie
                            val fechaIntercambioString = fechaIntercambioEdtxt.text.toString()
                            val fechaIntercambioParseada = if (fechaIntercambioString.isNotEmpty()) {
                                sdf.parse(fechaIntercambioString)
                            } else {
                                null
                            }

                            if (fechaIntercambioParseada == null) { //el campo de fecha de intercambio sigue vacio, entonces lo indicamos
                                // Si no se ha ingresado la fecha de intercambio, mostramos el error
                                binding.textInputLayoutFechaIntercambio.error = "Debe ingresar una fecha de intercambio"
                                isFormValid = false
                            } else { //si el campo si tiene un valor comprobamos
                                // Si la fecha de registro es posterior a la fecha del intercambio, mostramos el error
                                if (fechaRegistro.after(fechaIntercambioParseada)) {
                                    binding.textInputLayoutFechaRegistro.error = "11La fecha de registro no puede ser posterior a la fecha del intercambio"
                                    isFormValid = false
                                } else {
                                    // Si todo es correcto, eliminamos el error en ambos inputs
                                    binding.textInputLayoutFechaRegistro.error = null
                                    binding.textInputLayoutFechaIntercambio.error = null
                                    isFormValid = true
                                }
                            }
                        }
                    } catch (e: ParseException) { //NO deberia llegar a este error ya que el formato se lo da el datePicker
                        // Si hay un error al parsear la fecha, mostramos un error
                        binding.textInputLayoutFechaRegistro.error = "Formato de fecha inválido"
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación para la fecha de intercambio
        fechaIntercambioEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                // Validación del fecha limite de registro
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                if (input.isBlank()) {
                    binding.textInputLayoutFechaIntercambio.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    try {
                        // Parseamos la fecha del intercambio (fecha ingresada)
                        val fechaIntercambio = sdf.parse(input)

                        // Verificamos que la fecha de intercambio sea válida
                        if (fechaIntercambio != null) {
                            // Obtenemos la fecha de registro previamente ingresada
                            val fechaRegistroString = fechaRegistroEdtxt.text.toString()
                            val fechaRegistro = if (fechaRegistroString.isNotEmpty()) {
                                sdf.parse(fechaRegistroString)
                            } else {
                                null
                            }

                            if (fechaRegistro == null) {
                                // Si no se ha ingresado la fecha de registro, mostramos el error
                                binding.textInputLayoutFechaRegistro.error = "Debe ingresar una fecha de registro"
                                isFormValid = false
                            } else {
                                // Si la fecha de registro es posterior a la fecha de intercambio, mostramos el error
                                if (fechaRegistro.after(fechaIntercambio)) {
                                    binding.textInputLayoutFechaIntercambio.error = "La fecha de registro no puede ser posterior a la fecha del intercambio"
                                    isFormValid = false
                                } else {
                                    // Si todo es correcto, eliminamos el error en ambos inputs
                                    binding.textInputLayoutFechaIntercambio.error = null
                                    binding.textInputLayoutFechaRegistro.error = null
                                    isFormValid = true
                                }
                            }
                        }
                    } catch (e: ParseException) {
                        // Si no es una fecha válida
                        binding.textInputLayoutFechaRegistro.error = "Formato de fecha inválido"
                        isFormValid = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación de la hora
        horaIntercambioEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutHoraIntercambio.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    binding.textInputLayoutHoraIntercambio.error = null
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //Verificación del lugar
        lugarIntercambioEdtxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Aquí va la validación en tiempo real
                val input = s.toString()
                if (input.isBlank()) { //Si se vacia el campo
                    binding.textInputLayoutLugarIntercambio.error = "Este campo es obligatorio"
                    isFormValid = false
                } else {
                    binding.textInputLayoutLugarIntercambio.error = null
                    isFormValid = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }


    private fun inicializaSpinners() {                                                                                                          
        // Obtiene el array de colores desde strings.xml
        val colors = resources.getStringArray(R.array.color_items)
        // Configura el adaptador personalizado
        val adapter = ColorSpinnerAdapter(requireActivity(), colors.toList())
        spinnerColor.adapter = adapter

        //Spinner color
        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedcolor = parent?.getItemAtPosition(position).toString()
                view?.setBackgroundColor(Color.parseColor(selectedcolor))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedcolor = parent?.getItemAtPosition(0).toString() //Por default tiene el primer color
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTimePickerDialog(editText: EditText) {
        val timePicker = TimePickerFragment { time -> onTimeSelected(time, editText) }
        timePicker.show(childFragmentManager, "timePicker")
    }

    private fun onTimeSelected(time: String, editText: EditText) {
        editText.setText(time)
    }

    private fun showDatePickerDialog(editText: EditText) {
        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year, editText) }
        datePicker.show(childFragmentManager, "datePicker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int, editText: EditText) {
        // Crear un objeto Calendar para construir el Timestamp
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0) // Resta 1 al mes porque Calendar.MONTH es cero-indexado
            set(Calendar.MILLISECOND, 0)
        }

        // Formato de fecha: YYYY-MM-DD
        val formattedDate = getString(R.string.formatted_date, year, month, day)
        editText.setText(formattedDate)
    }


}