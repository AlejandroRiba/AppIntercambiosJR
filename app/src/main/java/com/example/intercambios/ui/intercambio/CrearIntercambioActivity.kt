package com.example.intercambios.ui.intercambio

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.intercambios.R
import com.example.intercambios.data.models.Intercambio
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.example.intercambios.utils.ColorSpinnerAdapter
import com.example.intercambios.utils.DatePickerFragment
import com.example.intercambios.utils.FormularioValidator
import com.example.intercambios.utils.GeneralUtils
import com.example.intercambios.utils.TimePickerFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class CrearIntercambioActivity : AppCompatActivity() {
    private lateinit var binding: NuevoIntercambioBinding

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

    private var selectedTheme: String? = null

    private var isFormValid by Delegates.notNull<Boolean>()

    private lateinit var genUtils: GeneralUtils
    private lateinit var intercambioUtils: IntercambioRepository
    private lateinit var usersUtils: UsersRepository

    private val selectedParticipants = mutableListOf<Participante>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openContactPicker()
            } else {
                showAlert("Es necesario otorgar permisos para acceder a los contactos.")
            }
        }

    private val pickContactLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val contactUri: Uri? = result.data?.data
                if (contactUri != null) {
                    handleContactSelection(contactUri)
                }
            }
        }

    private val selectedThemes = mutableListOf<String>()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val emailUsr = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NuevoIntercambioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        genUtils = GeneralUtils(this)
        intercambioUtils = IntercambioRepository()
        usersUtils = UsersRepository()

        initializeUI()
    }

    private fun initializeUI() {
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

        inicializaSpinners()

        //Guardo al usuario actual como organizador
        val participante = Participante(
            uid = userId!!,
            email = emailUsr!!,
            asignadoA = "",
            activo = true
        ) // Se agrega al organizador
        selectedParticipants.add(participante)

        horaIntercambioEdtxt.setOnClickListener { showTimePickerDialog(horaIntercambioEdtxt) }
        fechaIntercambioEdtxt.setOnClickListener { showDatePickerDialog(fechaIntercambioEdtxt) }
        fechaRegistroEdtxt.setOnClickListener { showDatePickerDialog(fechaRegistroEdtxt) }

        val validator = FormularioValidator(binding, this)
        validator.validarFormulario()

        binding.btnAddParticipant.setOnClickListener {
            checkAndRequestContactPermission()
        }

        botonGuardar.setOnClickListener {
            if (!isConnectedToInternet()) {
                genUtils.showAlert("No tienes conexión a Internet. Por favor, verifica e intenta de nuevo.")
                return@setOnClickListener
            }

            if (validator.getValid()) {
                sendFeedBack()
            } else {
                genUtils.showAlert("No se pudo enviar el formulario. Verifica de nuevo.")
            }
        }

        botonCancelar.setOnClickListener {
            finish()
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun checkAndRequestContactPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                openContactPicker()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_CONTACTS
            ) -> {
                showAlert("Es necesario otorgar permisos para acceder a los contactos.")
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    private fun handleContactSelection(contactUri: Uri) {
        val cursor = contentResolver.query(
            contactUri,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasEmail =
                    it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0

                if (hasEmail) {
                    val email = getContactEmail(id)
                    if (email != null) {
                        addParticipant(name, email)
                    } else {
                        showAlert("El contacto seleccionado ($name) no tiene un email asociado.")
                    }
                } else {
                    showAlert("El contacto seleccionado ($name) no tiene un email asociado.")
                }
            }
        }
    }

    private fun getContactEmail(contactId: String): String? {
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        emailCursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA))
            }
        }

        return null
    }

    private fun addParticipant(name: String, email: String) {
        // Llamas a la función para obtener el usuario por email
        usersUtils.obtenerUsuarioPorEmail(email).addOnSuccessListener {  (usuario, uid)  ->
            // Si el usuario existe, llenas los campos con los datos del usuario
            val participante = Participante(
                uid = uid,  // Asegúrate de que la clase Usuario tenga un campo `uid`
                email = usuario.email,  // Asegúrate de que la clase Usuario tenga un campo `email`
                temaRegalo = "",  // Asegúrate de que la clase Usuario tenga un campo `temaRegalo`
                asignadoA = "",  // Asegúrate de que la clase Usuario tenga un campo `asignadoA`
                activo = false  // Asegúrate de que la clase Usuario tenga un campo `activo`
            )
            if (!selectedParticipants.any { it.email == participante.email }) {
                selectedParticipants.add(participante)
                addParticipantChip(name, participante)
            } else {
                Toast.makeText(this, "El participante ya ha sido agregado.", Toast.LENGTH_SHORT).show()
            }
            // Aquí puedes hacer algo con el objeto participante, como llenar campos en la interfaz
        } .addOnFailureListener {
            // Si no se encuentra el usuario o ocurre un error, inicializas un objeto Participante por defecto
            val participante = Participante(
                uid = "",
                email = email,
                temaRegalo = "",
                asignadoA = "",
                activo = false
            )
            if (!selectedParticipants.any { it.email == participante.email }) {
                selectedParticipants.add(participante)
                addParticipantChip(name, participante)
            } else {
                Toast.makeText(this, "El participante ya ha sido agregado.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addParticipantChip(name: String, participante: Participante) {
        val chip = Chip(this)
        chip.text = name
        chip.isCloseIconVisible = true
        val chipDrawable =
            ChipDrawable.createFromAttributes(this, null, 0, R.style.CustomChip)
        chip.setChipDrawable(chipDrawable)
        chip.setOnCloseIconClickListener {
            binding.chipGroupInvitados.removeView(chip)
            selectedParticipants.remove(participante)
        }
        binding.chipGroupInvitados.addView(chip)
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun sendFeedBack() {
        nombre = nombreEdtxt.text.toString()
        personas = personasEdtxt.text.toString()
        montoMax = montoMaxEdtxt.text.trim().toString()
        fechaIntercambio = fechaIntercambioEdtxt.text.toString()
        fechaRegistro = fechaRegistroEdtxt.text.toString()
        descripcion = descripcionEdtxt.text.toString()
        horaIntercambio = horaIntercambioEdtxt.text.toString()
        lugarIntercambio = lugarIntercambioEdtxt.text.toString()
        if (nombre.isNotEmpty() && personas.isNotEmpty() && montoMax.isNotEmpty() && fechaIntercambio.isNotEmpty() && fechaRegistro.isNotEmpty() && horaIntercambio.isNotEmpty() && lugarIntercambio.isNotEmpty()) {
            val unicode = genUtils.generarCodigoUnicoConHash()
            if (descripcion.isEmpty())
                descripcion = getString(R.string.no_descripcion)

            val newIntercambio = Intercambio(
                code = unicode,
                nombre = nombre,
                monto = montoMax.toDouble(),
                numPersonas = personas.toInt(),
                descripcion = descripcion,
                fechaMaxRegistro = fechaRegistro,
                fechaIntercambio = fechaIntercambio,
                horaIntercambio = horaIntercambio,
                lugarIntercambio = lugarIntercambio,
                color = selectedcolor,
                personasRegistradas = selectedParticipants.size,
                participantes = listOf(),
                temas = selectedThemes,
                organizador = userId ?: "",
                sorteo = false
            )
            showThemesDialog(newIntercambio)
        } else {
            genUtils.showAlert("Es necesario rellenar todos los campos.")
        }
    }

    private fun inicializaSpinners() {
        // Obtiene el array de colores desde strings.xml
        val colors = resources.getStringArray(R.array.color_items)
        // Configura el adaptador personalizado
        val adapter = ColorSpinnerAdapter(this, colors.toList())
        spinnerColor.adapter = adapter

        //Spinner color
        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                selectedcolor =
                    parent?.getItemAtPosition(0).toString() //Por default tiene el primer color
            }

        }

        //Obtener referencia a la lista y campo de "temas"
        val spinnerThemes: Spinner = binding.spinnerThemes
        // Cargar temas desde el string-array
        val themes = resources.getStringArray(R.array.themes_array).toMutableList()
        val temasAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, themes)
        temasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerThemes.adapter = temasAdapter

        // Manejar selección de temas
        spinnerThemes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
                    Toast.makeText(
                        this@CrearIntercambioActivity,
                        "El tema ya está seleccionado",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@CrearIntercambioActivity,
                        "Solo puedes seleccionar hasta 3 temas",
                        Toast.LENGTH_SHORT
                    ).show()
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
        val chip = Chip(this)
        chip.text = theme
        chip.isCloseIconVisible = true
        val chipDrawable =
            ChipDrawable.createFromAttributes(this, null, 0, R.style.CustomChip)
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

        AlertDialog.Builder(this)
            .setTitle("Seleccionar tu tema de interés.")
            .setSingleChoiceItems(selectedThemes.toTypedArray(), currentSelectedIndex) { _, which ->
                tempSelectedIndex = which // Actualiza el índice temporalmente
            }
            .setPositiveButton("Aceptar") { _, _ ->
                // Asigna el tema seleccionado a la variable
                selectedTheme = selectedThemes[tempSelectedIndex]
                Toast.makeText(
                    this,
                    "Seleccionaste: $selectedTheme",
                    Toast.LENGTH_SHORT
                ).show()
                sendData(intercambio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sendData(intercambio: Intercambio) {
        if (selectedTheme != null && userId != null) {
            val indice = selectedParticipants.indexOfFirst { it.uid == userId }
            if (indice != -1) {
                // Actualizar solo el temaRegalo del participante
                selectedParticipants[indice] = selectedParticipants[indice].copy(temaRegalo = selectedTheme!!)
            }
            val updatedIntercambio = intercambio.copy(
                participantes = selectedParticipants
            )

            Log.i("CrearIntercambio", updatedIntercambio.toString())

            lifecycleScope.launch {
                val exito = intercambioUtils.addIntercambio(updatedIntercambio)
                withContext(Dispatchers.Main) {
                    if (exito) {
                        Log.i("CrearIntercambio", "Intercambio guardado correctamente en Firestore")
                        finish() // Cierra la actividad
                    } else {
                        Log.e("CrearIntercambio", "Error al guardar el intercambio en Firestore")
                    }
                }
            }
        } else {
            genUtils.showAlert("No se pudo enviar el formulario. Verifica de nuevo.")
        }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val timePicker = TimePickerFragment { time -> onTimeSelected(time, editText) }
        timePicker.show(supportFragmentManager, "timePicker")
    }


    private fun onTimeSelected(time: String, editText: EditText) {
        editText.setText(time)
    }

    private fun showDatePickerDialog(editText: EditText) {
        val datePicker =
            DatePickerFragment { day, month, year -> onDateSelected(day, month, year, editText) }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun onDateSelected(day: Int, month: Int, year: Int, editText: EditText) {
        // Formato de fecha: YYYY-MM-DD
        val formattedDate = getString(R.string.formatted_date, year, month, day)
        editText.setText(formattedDate)
    }

}
