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
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.intercambios.R
import com.example.intercambios.data.firebase.EmailSender
import com.example.intercambios.data.models.Intercambio
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.data.models.Usuario
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.example.intercambios.utils.ColorSpinnerAdapter
import com.example.intercambios.utils.DatePickerFragment
import com.example.intercambios.utils.FormularioValidator
import com.example.intercambios.utils.GeneralUtils
import com.example.intercambios.utils.SortManager.cancelarAlarmaSorteo
import com.example.intercambios.utils.SortManager.configurarAlarmaSorteo
import com.example.intercambios.utils.TimePickerFragment
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarIntercambio : AppCompatActivity() {

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


    private lateinit var nombre: String
    private lateinit var personas: String
    private lateinit var descripcion: String
    private var selectedThemes = mutableListOf<String>() // Lista vacía inicialmente para los nuevos temas
    private var selectedThemesConsult = mutableListOf<String>() //Lista vacía inicialmente para los cachados de firebase
    private lateinit var montoMax: String
    private lateinit var fechaRegistro: String
    private lateinit var fechaRespaldo: String
    private lateinit var fechaIntercambio: String
    private lateinit var horaIntercambio: String
    private lateinit var lugarIntercambio: String
    private lateinit var organizador: String
    private var selectedParticipants = mutableListOf<Participante>() //Lista de participantes
    private var selectedParticipantsCopia = mutableListOf<Participante>() //Lista de participantes
    private lateinit var selectedcolor: String
    private lateinit var botonGuardar: Button
    private lateinit var botonCancelar: Button

    private var selectedTheme: String? = null
    private lateinit var intercambioViejo: Intercambio

    private lateinit var genUtils: GeneralUtils
    private lateinit var intercambioUtils: IntercambioRepository
    private lateinit var usersUtils: UsersRepository


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openContactPicker()
            } else {
                genUtils.showAlert(getString(R.string.permisos_contatactos))
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

    private lateinit var docID: String

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NuevoIntercambioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        genUtils = GeneralUtils(this)
        intercambioUtils = IntercambioRepository()
        usersUtils = UsersRepository()
        docID = intent.getStringExtra("docId") ?: ""
        if(docID.isNotEmpty()) {
            Log.i("EditarIntercambio", docID)
            consultarFirebase(docID) //busqueda de intercambio en firebase
        }else{
            genUtils.showAlertandFinish(getString(R.string.no_encontrado_email), getString(R.string.error_title))
        }
    }

    private fun consultarFirebase(docID: String){
        intercambioUtils.obtenerIntercambioPorId(docID)
            .addOnSuccessListener { intercambio ->
                intercambioViejo = intercambio
                nombre = intercambio.nombre
                selectedThemes = intercambio.temas.toMutableList() // Asignar la lista de temas al valor de selectedThemes
                selectedThemesConsult = intercambio.temas.toMutableList() //Lo usamos como auxiliar para guardar una copia de datos
                fechaIntercambio = intercambio.fechaIntercambio
                horaIntercambio = intercambio.horaIntercambio
                lugarIntercambio = intercambio.lugarIntercambio
                montoMax = intercambio.monto.toString()
                fechaRegistro = intercambio.fechaMaxRegistro
                fechaRespaldo = intercambio.fechaMaxRegistro
                personas = intercambio.numPersonas.toString()
                descripcion = intercambio.descripcion
                organizador = intercambio.organizador
                selectedcolor = intercambio.color
                Log.i("EditarIntercambio", "Color seleccionado : $selectedcolor")
                selectedParticipants = intercambio.participantes.toMutableList()
                selectedParticipantsCopia = intercambio.participantes.toMutableList()
                initializeUI() //referencias a componentes
            }.addOnFailureListener{
                genUtils.showAlertandFinish(getString(R.string.no_encontrado_email), getString(R.string.error_title))
            }
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

        val tituloTxt = binding.formTitleTextView
        tituloTxt.text = getString(R.string.editar_intercambio)
        botonGuardar.text = getString(R.string.guardar)
        botonCancelar.text = getString(R.string.cancelar)

        inicializaSpinners()
        //Asignacion de valores
        nombreEdtxt.text = Editable.Factory.getInstance().newEditable(nombre)
        personasEdtxt.text = Editable.Factory.getInstance().newEditable(personas)
        descripcionEdtxt.text = Editable.Factory.getInstance().newEditable(descripcion)
        montoMaxEdtxt.text = Editable.Factory.getInstance().newEditable(montoMax)
        fechaRegistroEdtxt.text = Editable.Factory.getInstance().newEditable(fechaRegistro)
        fechaIntercambioEdtxt.text = Editable.Factory.getInstance().newEditable(fechaIntercambio)
        horaIntercambioEdtxt.text = Editable.Factory.getInstance().newEditable(horaIntercambio)
        lugarIntercambioEdtxt.text = Editable.Factory.getInstance().newEditable(lugarIntercambio)
        //cargar participantes
        loadParticipants()
        loadThemes()


        horaIntercambioEdtxt.setOnClickListener { showTimePickerDialog(horaIntercambioEdtxt) }
        fechaIntercambioEdtxt.setOnClickListener { showDatePickerDialog(fechaIntercambioEdtxt) }
        fechaRegistroEdtxt.setOnClickListener { showDatePickerDialog(fechaRegistroEdtxt) }

        val validator = FormularioValidator(binding, this)
        validator.validarFormulario()

        binding.btnAddParticipant.setOnClickListener {
            personas = personasEdtxt.text.trim().toString()
            if(personas.isNotBlank() && personas.toInt() > 1){
                showInvitationDialog()
            }else{
                genUtils.showAlert(getString(R.string.personasnecesarias))
            }
        }

        botonGuardar.setOnClickListener {
            if (!isConnectedToInternet()) {
                genUtils.showAlert(getString(R.string.envio_denegado_conexion))
                return@setOnClickListener
            }

            if (validator.getValid()) {
                sendFeedBack()
            } else {
                Log.i("EditarIntercambio", "No es validado")
                genUtils.showAlert(getString(R.string.envio_denegado_campos_vacios))
            }
        }

        botonCancelar.setOnClickListener {
            finish()
        }
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
        if (nombre.isNotEmpty() && personas.isNotEmpty() && montoMax.isNotEmpty() && fechaIntercambio.isNotEmpty() && fechaRegistro.isNotEmpty() && horaIntercambio.isNotEmpty() && lugarIntercambio.isNotEmpty() && selectedThemes.isNotEmpty()) {
            if(personas.toInt() < selectedParticipants.size){
                genUtils.showAlert(getString(R.string.inconsistencia_participantes))
                return
            }

            if (descripcion.isEmpty())
                descripcion = getString(R.string.no_descripcion)

            val updatedIntercambio = intercambioViejo.copy(
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
                participantes = selectedParticipants,
                temas = selectedThemes,
            )
            Log.i("EditarIntercambio", updatedIntercambio.toString())
            val newThemesSet = selectedThemes.toSet()
            val oldThemesSet = selectedThemesConsult.toSet()

            // Temas agregados
            val addedThemes = newThemesSet - oldThemesSet
            // Temas eliminados
            val removedThemes = oldThemesSet - newThemesSet

            Log.i("EditarIntercambio", "Temas quitados : $removedThemes")
            Log.i("EditarIntercambio", "Temas agregados : $addedThemes")
            selectedParticipants.forEach{ participante ->
                if(participante.temaRegalo in removedThemes && participante.uid != organizador){
                    participante.temaRegalo = ""
                }
            }

            val userActual = selectedParticipants.find { participante -> participante.uid == organizador }
            if(userActual != null){
                val userActualTema = userActual.temaRegalo
                if(userActualTema in removedThemes){
                    Log.i("EditarIntercambio", "El tema actual esta en los temas que se quitaron, es necesaria una actualización.")
                    showThemesDialog(updatedIntercambio)
                }else{
                    Log.i("EditarIntercambio", "Pasa a la actualización.")
                    selectedTheme = userActual.temaRegalo
                    sendData(updatedIntercambio, false)
                }
            }
        } else {
            genUtils.showAlert(getString(R.string.envio_denegado_campos_vacios))
        }
    }

    private fun isConnectedToInternet(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showInvitationDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_invitacion_opciones, null)
        // Crear y configurar el AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnInvitarContacto).setOnClickListener {
            checkAndRequestContactPermission()
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnManual).setOnClickListener {
            showManualDialog()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showManualDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_datos_invitado, null)
        val nombreInvitado = dialogView.findViewById<EditText>(R.id.editTextNombreInv)
        val correoInvitado = dialogView.findViewById<EditText>(R.id.editTextCorreo)
        // Crear y configurar el AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            if(nombreInvitado.text.isNotBlank() && correoInvitado.text.isNotBlank()){
                addParticipant(nombreInvitado.text.toString(), correoInvitado.text.toString())
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
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
                genUtils.showAlert(getString(R.string.permisos_contatactos))
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

                val email = getContactEmail(id)
                if (email != null) {
                    personas = personasEdtxt.text.toString()
                    if(selectedParticipants.size < personas.toInt()){
                        val userExistente = selectedParticipantsCopia.find { participante -> participante.email == email }
                        if(userExistente !=  null){ //Significa que ya estaba en la lista
                            selectedParticipants.add(userExistente) //solo copia sus datos que ya estaban antes
                            addParticipantChip(name, userExistente)
                        }else{ //nunca estuvo en la lista
                            addParticipant(name, email) //Aquí tengo que manejar el método para el copiado de datos
                        }
                    }else{
                        genUtils.showAlert(getString(R.string.limitedepersonas))
                    }
                } else {
                    genUtils.showAlert(getString(R.string.contact_no_email, name))
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

    private fun inicializaSpinners() {
        // Obtiene el array de colores desde strings.xml
        val colors = resources.getStringArray(R.array.color_items)
        // Configura el adaptador personalizado
        val adapter = ColorSpinnerAdapter(this, colors.toList())
        spinnerColor.adapter = adapter

        val indexTemp = colors.indexOfFirst { it.equals(selectedcolor, ignoreCase = true) }
        // Configura el color predefinido
        spinnerColor.setSelection(if (indexTemp != -1) indexTemp else 0)

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
                        this@EditarIntercambio,
                        getString(R.string.tema_seleccionado),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@EditarIntercambio,
                        getString(R.string.tres_temas),
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

    private fun loadParticipants() {
        val tasks = mutableListOf<Task<Pair<Participante, Usuario>>>()

        for (participante in selectedParticipants) {
            val task = usersUtils.obtenerUsuarioPorId(participante.uid)
                .onSuccessTask { usuario ->
                    Tasks.forResult(Pair(participante, usuario))
                }
                .addOnFailureListener {
                    tasks.add(Tasks.forResult(Pair(participante, Usuario())))
                }
            tasks.add(task)
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener { results ->
            @Suppress("UNCHECKED_CAST")
            val participantesInfo = results.mapNotNull { task ->
                if (task.isSuccessful) task.result as Pair<Participante, Usuario> else null
            }

            participantesInfo.forEach { (participante, usuario) ->
                // Excluir al organizador
                if (participante.uid != organizador) {
                    val alias = usuario.alias.ifEmpty { getString(R.string.no_registrado) }
                    addParticipantChip(alias, participante)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("EditarIntercambio", "Error al obtener participantes: ${exception.message}")
        }
    }

    private fun loadThemes() {
        selectedThemes.forEach { theme ->
            addChip(theme)
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

        // Inflar el diseño personalizado
        val dialogView = layoutInflater.inflate(R.layout.dialog_selecciontema, null)

        // Obtener el RadioGroup para las opciones
        val selectionGroup = dialogView.findViewById<RadioGroup>(R.id.selectionGroup)

        // Agregar las opciones dinámicamente
        selectedThemes.forEachIndexed { index, option ->
            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                text = option
                textSize = 15f
                setTextAppearance(R.style.CustomRadioButton)
            }
            selectionGroup.addView(radioButton)
            if (index == tempSelectedIndex && tempSelectedIndex != -1) {
                radioButton.isChecked = true // Preseleccionar la opción definida
            }
        }

        // Crear y configurar el AlertDialog
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Configurar el botón de confirmación
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val selectedId = selectionGroup.checkedRadioButtonId
            if (selectedId == -1) {
                // No se ha seleccionado ninguna opción
                Toast.makeText(this, getString(R.string.selecciona_un_tema), Toast.LENGTH_SHORT).show()
            } else {
                // Se ha seleccionado una opción
                val selectedRadioButton = dialogView.findViewById<RadioButton>(selectedId)
                val selectedOption = selectedRadioButton?.text?.toString()

                if (!selectedOption.isNullOrBlank()) {
                    tempSelectedIndex = selectedThemes.indexOf(selectedOption)
                    if (tempSelectedIndex != -1) {
                        selectedTheme = selectedThemes[tempSelectedIndex]
                        Log.i("EditarIntercambio", selectedTheme!!)
                        sendData(intercambio, true)
                    } else {
                        Toast.makeText(this, getString(R.string.selecciona_un_tema), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.selecciona_un_tema), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Mostrar el diálogo
        alertDialog.show()
    }

    private fun sendData(intercambio: Intercambio, flagCambioTema: Boolean) {
        if (selectedTheme != null && userId != null) {
            var updatedIntercambio = intercambio
            if(flagCambioTema){
                val indice = selectedParticipants.indexOfFirst { it.uid == userId }
                if (indice != -1) {
                    // Actualizar solo el temaRegalo del participante
                    selectedParticipants[indice] = selectedParticipants[indice].copy(temaRegalo = selectedTheme!!)
                }
                updatedIntercambio = intercambio.copy(
                    participantes = selectedParticipants
                )
            }

            Log.i("CrearIntercambio", updatedIntercambio.toString())

            lifecycleScope.launch {
                val exito = intercambioUtils.actualizarIntercambio(updatedIntercambio, docID)
                withContext(Dispatchers.Main) {
                    if (exito) {
                        cancelarAlarmaSorteo(this@EditarIntercambio, docID)
                        configurarAlarmaSorteo(this@EditarIntercambio, fechaRegistro, docID)
                        usersUtils.obtenerUsuarioPorId(userId).addOnSuccessListener { organizadorUser ->
                            intercambioUtils.generarEnlaceDinamico(intercambio.code) { link ->
                                if (link != null) {
                                    enviarCorreoAListaDeEmails(updatedIntercambio.code, updatedIntercambio.nombre, organizadorUser.nombre, organizadorUser.email, link)
                                    Log.i("CrearIntercambio", "Intercambio guardado correctamente en Firestore")
                                    finish() // Cierra la actividad
                                } else {
                                    Log.e("CrearIntercambio", "Error al generar el enlace dinámico")
                                }
                            }
                        }.addOnFailureListener{
                            finish()
                        }
                    } else {
                        Log.e("CrearIntercambio", "Error al guardar el intercambio en Firestore")
                    }
                }
            }
        } else {
            genUtils.showAlert(getString(R.string.envio_denegado_campos_vacios))
        }
    }

    private fun enviarCorreoAListaDeEmails(codigo: String, nombreIntercambio: String, organizador: String, organizadorMail: String, link: String) {
        // Crea un array de los correos que quieres enviar la invitación
        //personas agregadas
        val newParticipants = selectedParticipants.toSet()
        val oldParticipants = selectedParticipantsCopia.toSet()

        // Temas agregados
        val addedParticipants = newParticipants - oldParticipants
        val emailList = addedParticipants // Lista de correos
        val emailSender = EmailSender()
        // Llama a la función Firebase Functions para enviar el correo
        emailList.forEach { participante ->
            if(participante.email != organizadorMail)
                emailSender.enviarCorreoSMTP(participante.email, organizador, codigo, nombreIntercambio, link, this)
        }
        selectedParticipantsCopia.forEach{ participante ->
            if(participante.temaRegalo.isBlank()){
                emailSender.notificacionCambioTemas(participante.email, organizador, codigo, nombre, link, this) //se envia el nombre viejo
            }
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