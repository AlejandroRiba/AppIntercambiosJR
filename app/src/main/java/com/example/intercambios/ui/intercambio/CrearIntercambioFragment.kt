package com.example.intercambios.ui.intercambio

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.intercambios.R
import com.example.intercambios.data.models.Intercambio
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.example.intercambios.utils.ColorSpinnerAdapter
import com.example.intercambios.utils.DatePickerFragment
import com.example.intercambios.utils.FormularioValidator
import com.example.intercambios.utils.GeneralUtils
import com.example.intercambios.utils.TimePickerFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
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
    private lateinit var botonCancelar: Button

    // Variable donde se guarda el tema seleccionado
    private var selectedTheme: String? = null

    private var isFormValid by Delegates.notNull<Boolean>()

    private lateinit var genUtils: GeneralUtils
    private lateinit var intercambioUtils: IntercambioRepository

    private val selectedThemes = mutableListOf<String>()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

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
        intercambioUtils = IntercambioRepository()

        nombreEdtxt = binding.edTextNombre
        personasEdtxt = binding.edTextNumPersonas
        descripcionEdtxt = binding.exchangeDescriptionEditText
        montoMaxEdtxt = binding.edTextMontoMax
        fechaRegistroEdtxt = binding.edTextFechaRegistro
        fechaIntercambioEdtxt = binding.edTextFechaIntercambio
        horaIntercambioEdtxt = binding.edTextHora
        lugarIntercambioEdtxt = binding.edTextLugar
        botonGuardar = binding.createExchangeButton
        botonCancelar = binding.btnSaltar
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

        botonCancelar.setOnClickListener{
            backHome()
        }

    }

    private fun backHome(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Código para Android 13 (API 33) o superior
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            // Código para versiones inferiores a Android 13
            requireActivity().onBackPressed()
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

            val newIntercambio = Intercambio(
                code = unicode,
                nombre = nombre,
                numPersonas = personas.toInt(),
                descripcion = descripcion,
                fechaMaxRegistro = fechaRegistro,
                fechaIntercambio = fechaIntercambio,
                horaIntercambio = horaIntercambio,
                lugarIntercambio = lugarIntercambio,
                color = selectedcolor,
                personasRegistradas = 1, //solo se registra la persona que lo crea
                participantes = listOf(),
                temas = selectedThemes,
                organizador = userId ?: ""
            )
            Log.i("CrearIntercambio", newIntercambio.toString())
            showThemesDialog(newIntercambio)
            /**/

        }else{
            genUtils.showAlert("Es necesario rellenar todos los campos.")
        }
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

        //Obtener referencia a la lista y campo de "temas"
        val spinnerThemes: Spinner = binding.spinnerThemes
        // Cargar temas desde el string-array
        val themes = resources.getStringArray(R.array.themes_array).toMutableList()
        val temasAdapter =  ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, themes)
        temasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerThemes.adapter = temasAdapter

        // Manejar selección de temas
        spinnerThemes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val theme = parent?.getItemAtPosition(position).toString()

                if (position == 0) {
                    // Ignorar la opción inicial
                    return
                }

                Log.i("CrearIntercambio", "Se selecciono $theme")

                if (selectedThemes.size < 3 && !selectedThemes.contains(theme)) {
                    selectedThemes.add(theme)
                    Log.i("CrearIntercambio", selectedThemes.toString())
                    addChip(theme)
                } else if (selectedThemes.contains(theme)) {
                    Toast.makeText(requireActivity(), "El tema ya está seleccionado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(), "Solo puedes seleccionar hasta 3 temas", Toast.LENGTH_SHORT).show()
                }
                spinnerThemes.setSelection(0) //reinicio visual del spinner
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }

    }

    //Función para agregar chips a la lista
    private fun addChip(theme: String) {
        val chip = Chip(requireActivity())
        chip.text = theme
        chip.isCloseIconVisible = true
        val chipDrawable =
            ChipDrawable.createFromAttributes(requireActivity(), null, 0, R.style.CustomChip)
        chip.setChipDrawable(chipDrawable)
        chip.setOnCloseIconClickListener {
            binding.chipGroupSelected.removeView(chip)
            selectedThemes.remove(theme)
            Log.i("CrearIntercambio", selectedThemes.toString())
        }
        binding.chipGroupSelected.addView(chip)
    }


    //Mostrar
    private fun showThemesDialog(intercambio: Intercambio) {
        // Crear el diálogo
        // Índice del tema seleccionado actual
        val currentSelectedIndex = selectedThemes.indexOf(selectedTheme)
        var tempSelectedIndex = currentSelectedIndex // Variable temporal para actualizar al aceptar

        AlertDialog.Builder(requireActivity())
            .setTitle("Seleccionar tu tema de interés.")
            .setSingleChoiceItems(selectedThemes.toTypedArray(), currentSelectedIndex) { _, which ->
                tempSelectedIndex = which // Actualiza el índice temporalmente
            }
            .setPositiveButton("Aceptar") { _, _ ->
                // Asigna el tema seleccionado a la variable
                selectedTheme = selectedThemes[tempSelectedIndex]
                Toast.makeText(requireActivity(), "Seleccionaste: $selectedTheme", Toast.LENGTH_SHORT).show()
                sendData(intercambio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sendData(intercambio: Intercambio) {
        CoroutineScope(Dispatchers.IO).launch {
            val exito = intercambioUtils.addIntercambio(intercambio)
            if (exito) {
                intercambioUtils.generarEnlaceDinamico(intercambio.code) { link ->
                    if (link != null) {
                        enviarCorreo(link, intercambio.nombre)
                    } else {
                        Log.e("CrearIntercambio", "Error al generar el enlace dinámico")
                    }
                }
            }
        }
    }

    private fun enviarCorreo(enlace: String, nombreIntercambio: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Invitación a un intercambio")
        intent.putExtra(Intent.EXTRA_TEXT, "Te invito al intercambio: $nombreIntercambio. Únete usando este enlace: $enlace")
        startActivity(Intent.createChooser(intent, "Enviar invitación"))
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
        // Formato de fecha: YYYY-MM-DD
        val formattedDate = getString(R.string.formatted_date, year, month, day)
        editText.setText(formattedDate)
    }
}