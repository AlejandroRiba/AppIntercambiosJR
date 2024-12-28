package com.example.intercambios.ui.intercambio

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.intercambios.R
import com.example.intercambios.data.models.Intercambio
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.data.models.Usuario
import com.example.intercambios.utils.DateUtils.dateFormatting
import com.example.intercambios.utils.DateUtils.isDatePast
import com.example.intercambios.utils.GeneralUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DetalleIntercambio : AppCompatActivity() {

    private lateinit var nombre: TextView
    private lateinit var code: TextView
    private lateinit var numPersonas: TextView
    private lateinit var descripcion: TextView
    private lateinit var fechaMaxRegistro: TextView
    private lateinit var fechaIntercambio: TextView
    private lateinit var horaIntercambio: TextView
    private lateinit var lugarIntercambio: TextView
    private lateinit var montoIntercambio: TextView
    private lateinit var color: View
    private lateinit var participantes: TextView
    private lateinit var temas: TextView
    private lateinit var organizador: TextView

    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: ImageButton
    private lateinit var btnAdelentar: Button
    private lateinit var btnDelete: Button
    private lateinit var btnUnirme: Button
    private lateinit var btnTema: Button
    private lateinit var btnConsultaSort: Button
    private lateinit var btnRechazar: Button
    private lateinit var btnInvitacion: Button
    private var autorizaSalir: Boolean = true
    private var sorteoRealizado = false
    private var autorizaAdelantarSorteo = false
    private var unirNuevoUser: Boolean = false
    private var actualIsOwner: Boolean = false
    private var isDateExpired: Boolean = false
    private lateinit var docID: String
    private lateinit var codigo: String

    private var selectedThemes = mutableListOf<String>() // Lista vacía inicialmente


    private lateinit var genUtils: GeneralUtils

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email

    private lateinit var intercambioUtils: IntercambioRepository
    private val usersUtils = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_intercambio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        genUtils = GeneralUtils(this)
        intercambioUtils = IntercambioRepository()
        docID = intent.getStringExtra("docId") ?: ""
        codigo = intent.getStringExtra("codigo") ?: ""
        unirNuevoUser = intent.getBooleanExtra("union", false)

        //Referencias a los componenetes
        btnBack = findViewById(R.id.back)
        btnEdit = findViewById(R.id.edit)
        btnAdelentar = findViewById(R.id.btn_adelantar)
        btnDelete = findViewById(R.id.btn_delete)
        btnUnirme = findViewById(R.id.btn_unirme)
        btnTema = findViewById(R.id.btn_selecciontema)
        btnRechazar = findViewById(R.id.btn_rechazar)
        btnInvitacion = findViewById(R.id.btn_invitar)
        btnConsultaSort = findViewById(R.id.btn_consultarsorteo)
        btnUnirme.visibility = View.GONE //INICIALMENTE OCULTOS
        btnAdelentar.visibility = View.GONE
        btnInvitacion.visibility = View.GONE
        btnEdit.visibility = View.GONE
        btnTema.visibility = View.GONE
        btnDelete.visibility = View.GONE
        btnConsultaSort.visibility = View.GONE
        btnRechazar.visibility = View.GONE
        nombre = findViewById(R.id.tex1)
        fechaIntercambio = findViewById(R.id.tex2)
        horaIntercambio = findViewById(R.id.tex3)
        lugarIntercambio = findViewById(R.id.tex4)
        montoIntercambio = findViewById(R.id.montotext)
        code = findViewById(R.id.tex5)
        fechaMaxRegistro = findViewById(R.id.tex6)
        color = findViewById(R.id.colorRef)
        organizador = findViewById(R.id.tex7)
        temas = findViewById(R.id.tex8)
        participantes = findViewById(R.id.tex9)
        numPersonas = findViewById(R.id.texA)
        descripcion = findViewById(R.id.texB)

        btnBack.setOnClickListener { finish() }
        btnEdit.setOnClickListener {
            val intentEdit = Intent(this, EditarIntercambio::class.java).apply {
                putExtra("docId", docID)
            }
            startActivity(intentEdit)
        }

        btnDelete.setOnClickListener {
            if(autorizaSalir){
                if(actualIsOwner){
                    intercambioUtils.eliminarIntercambioPorId(docID).addOnSuccessListener { success ->
                        if(success){
                            genUtils.showAlertandFinish(getString(R.string.success_message), getString(R.string.success_salida_titulo))
                        }else{
                            genUtils.showAlertandFinish(getString(R.string.error_eliminar), getString(R.string.error_title))
                        }
                    }.addOnFailureListener{
                        genUtils.showAlertandFinish(getString(R.string.error_eliminar), getString(R.string.error_title))
                    }
                }else if(userEmail != null){
                    intercambioUtils.eliminarParticipante(docID,userEmail).addOnSuccessListener { success ->
                        if(success){
                            genUtils.showAlertandFinish(getString(R.string.success_salir_exchange), getString(R.string.success_salida_titulo))
                        }else{
                            genUtils.showAlertandFinish(getString(R.string.error_rechazo), getString(R.string.error_title))
                        }
                    }.addOnFailureListener{
                        genUtils.showAlertandFinish(getString(R.string.error_rechazo), getString(R.string.error_title))
                    }
                }
            }else{
                genUtils.showAlert(getString(R.string.warning_salir))
            }
        }

        btnConsultaSort.setOnClickListener {
            intercambioUtils.obtenerIntercambioPorId(docID).addOnSuccessListener { intercambio ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                val userActual = intercambio.participantes.find { it.uid == userId }
                if (userActual != null && !userActual.asignadoA.isNullOrEmpty()) {
                    val asignado = intercambio.participantes.find { it.uid == userActual.asignadoA }
                    val asignadoEmail = asignado?.email ?: "No asignado"

                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Intercambio asignado")
                        .setMessage("Se te asignó a: $asignadoEmail")
                        .setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    dialog.show()
                } else {
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Sin asignación")
                        .setMessage("Aún no tienes asignado a nadie para el intercambio.")
                        .setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    dialog.show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener los datos del intercambio.", Toast.LENGTH_SHORT).show()
            }
        }







        btnAdelentar.setOnClickListener {
            val sorteoIntent = Intent(this, SorteoActivity::class.java).apply {
                putExtra("docId", docID)
            }
            startActivity(sorteoIntent)

            intercambioUtils.obtenerIntercambioPorId(docID).addOnSuccessListener { intercambio ->
                val participantes = intercambio.participantes

                if (participantes.isNotEmpty()) {
                    // Realizar el sorteo
                    val participantesDisponibles = participantes.toMutableList()
                    val participantesAsignados = mutableListOf<Participante>()

                    participantes.forEach { participante ->
                        val disponiblesParaAsignar = participantesDisponibles.filter { it.uid != participante.uid }
                        if (disponiblesParaAsignar.isNotEmpty()) {
                            val asignado = disponiblesParaAsignar.random()
                            participantesAsignados.add(participante.copy(asignadoA = asignado.uid))
                            participantesDisponibles.remove(asignado) // Quitar asignado de la lista
                        }
                    }

                    // Crear el objeto actualizado de intercambio
                    val intercambioActualizado = intercambio.copy(participantes = participantesAsignados)

                    // Guardar el intercambio actualizado en Firebase
                    lifecycleScope.launch {
                        val result = intercambioUtils.actualizarIntercambio(intercambioActualizado, docID)
                        if (result) {
                            btnConsultaSort.visibility = View.VISIBLE // Mostrar el botón de consultar sorteo
                            Toast.makeText(this@DetalleIntercambio, "Sorteo realizado y guardado con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DetalleIntercambio, "Error al guardar el sorteo en Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "No hay participantes válidos para el sorteo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error al obtener el intercambio", Toast.LENGTH_SHORT).show()
            }
        }






        btnInvitacion.setOnClickListener {
            if(code.text.isNotBlank() && nombre.text.isNotBlank()){
                intercambioUtils.generarEnlaceDinamico(code.text.toString()) { link ->
                    if (link != null) {
                        enviarInvitacion(link, nombre.text.toString(), code.text.toString())
                    } else {
                        Log.e("CrearIntercambio", "Error al generar el enlace dinámico")
                    }
                }
            }else{
                genUtils.showAlert(getString(R.string.no_encontrado_email))
            }
        }

        btnRechazar.setOnClickListener {
            if (userEmail != null) {
                intercambioUtils.eliminarParticipante(docID,userEmail).addOnSuccessListener { success ->
                    if(success){
                        genUtils.showAlertandFinish(getString(R.string.invitacion_rechazada),getString(R.string.rechazo_titulo))
                    }else{
                        genUtils.showAlertandFinish(getString(R.string.error_rechazo), getString(R.string.error_title))
                    }
                }.addOnFailureListener{
                    genUtils.showAlertandFinish(getString(R.string.error_rechazo), getString(R.string.error_title))
                }
            }
        }

        btnUnirme.setOnClickListener {
            showThemesDialog(false)
        }

        btnTema.setOnClickListener {
            showThemesDialog(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if(docID.isNotEmpty()) {
            Log.i("DetalleIntercambio", docID)
            consultarFirebase(docID)
        }else{
            if(codigo.isNotBlank()){
                intercambioUtils.obtenerDocId(codigo)
                    .addOnSuccessListener { interID ->
                        // Si se encuentra el documento, llama a consultarFirebase
                        docID = interID
                        consultarFirebase(interID)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Detalle", "Mensaje de error $e")
                        genUtils.showAlertandFinish(getString(R.string.no_encontrado_email), getString(R.string.error_title))
                    }
            }else{
                finish() //si el codigo esta vacio y no hay docID entonces bye
            }
        }
    }

    private fun showThemesDialog(edit: Boolean) {
        // Inflar el diseño personalizado
        val dialogView = layoutInflater.inflate(R.layout.dialog_selecciontema, null)
        // Obtener el RadioGroup para las opciones
        val selectionGroup = dialogView.findViewById<RadioGroup>(R.id.selectionGroup)

        // Agregar las opciones dinámicamente
        selectedThemes.forEachIndexed { index, option ->
            val radioButton = layoutInflater.inflate(R.layout.radio_button, selectionGroup, false) as RadioButton
            radioButton.text = option
            radioButton.id = View.generateViewId()

            selectionGroup.addView(radioButton)
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
                    val tempSelectedIndex = selectedThemes.indexOf(selectedOption)
                    if (tempSelectedIndex != -1) {
                        alertDialog.dismiss()
                        if(edit){
                            editarParticipanteFirebase(selectedOption)
                        }else{
                            agregarParticipanteFirebase(selectedOption)
                        }
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

    private fun agregarParticipanteFirebase(selectedTheme: String){
        if(userEmail != null){
            intercambioUtils.agregarParticipante(docID,userEmail, selectedTheme).addOnSuccessListener { success ->
                if(success){
                    genUtils.showAlertandFinish(getString(R.string.union_message), getString(R.string.union_title))
                }else{
                    genUtils.showAlertandFinish(getString(R.string.error_union), getString(R.string.error_title))
                }
            }.addOnFailureListener{
                genUtils.showAlertandFinish(getString(R.string.error_union), getString(R.string.error_title))
            }
        }
    }

    private fun editarParticipanteFirebase(selectedTheme: String){
        if(userId != null){
            intercambioUtils.editarTemaRegalo(docID, selectedTheme).addOnSuccessListener { success ->
                if(success){
                    genUtils.showAlertandFinish(getString(R.string.tema_seleccionado_exito), getString(R.string.titulo_exito))
                }else{
                    genUtils.showAlertandFinish(getString(R.string.error_tema_selecc), getString(R.string.error_title))
                }
            }.addOnFailureListener{
                genUtils.showAlertandFinish(getString(R.string.error_tema_selecc), getString(R.string.error_title))
            }
        }
    }

    private fun consultarFirebase(docID: String){
        intercambioUtils.obtenerIntercambioPorId(docID)
            .addOnSuccessListener { intercambio ->
                findViewById<TextView>(R.id.tex9title).text = getString(R.string.cantidad_participantes, intercambio.personasRegistradas.toString(), intercambio.numPersonas.toString())
                nombre.text = intercambio.nombre
                selectedThemes = intercambio.temas.toMutableList() // Asignar la lista de temas al valor de selectedThemes
                fechaIntercambio.text = dateFormatting(this, intercambio.fechaIntercambio)
                horaIntercambio.text = intercambio.horaIntercambio
                lugarIntercambio.text = intercambio.lugarIntercambio
                montoIntercambio.text = getString(R.string.monto_intercambio, intercambio.monto)
                code.text = intercambio.code
                fechaMaxRegistro.text = dateFormatting(this, intercambio.fechaMaxRegistro)
                if(isDatePast(intercambio.fechaMaxRegistro)){
                    isDateExpired = true
                }
                numPersonas.text = getString(R.string.num_personas, intercambio.numPersonas)
                descripcion.text = intercambio.descripcion
                sorteoRealizado = intercambio.sorteo
                if(intercambio.sorteo && userId != intercambio.organizador){ //Si no eres el organizador y el sorteo ya se hizo no permite salir
                    autorizaSalir = false
                }

                val regViewBG = color.background
                try {
                    regViewBG.setTint(Color.parseColor(intercambio.color))
                } catch (e: IllegalArgumentException) {
                    // Si el color no es válido, usa un color por defecto
                    regViewBG.setTint(Color.parseColor("#FFFFFF")) // Blanco por defecto
                }
                temas.text = formatearTemas(intercambio.temas)

                val todosActivos = intercambio.participantes.find { participante -> !participante.activo || participante.temaRegalo.isBlank() }
                if(todosActivos == null && intercambio.personasRegistradas == intercambio.numPersonas){ //si no encuentra ninguno inactivo puede adelantar el sorteo
                    autorizaAdelantarSorteo = true
                }

                val userActual =  intercambio.participantes.find { participante -> participante.uid == userId }
                if((userActual == null && unirNuevoUser) || (userActual != null && unirNuevoUser && !userActual.activo)){ //se debe especificar en el intent
                    if(!isDateExpired){
                        btnUnirme.visibility = View.VISIBLE //MOSTRAMOS EL BOTÓN PARA UNIRSE
                        if(userActual != null){ //Si el participante ya está en la lista de participantes pero no ha aceptado
                            btnRechazar.visibility = View.VISIBLE
                        }
                    }
                }else{ //EN caso de que si lo encuentre
                    // Aquí puedes trabajar con el objeto Intercambio
                    if (intercambio.sorteo) {
                        val userActual = intercambio.participantes.find { participante -> participante.uid == userId }
                        if (userActual != null && !userActual.asignadoA.isNullOrEmpty() && intercambio.organizador != userId) {

                            btnConsultaSort.visibility = View.VISIBLE
                        } else {
                            btnConsultaSort.visibility = View.GONE
                        }
                    }

                    if(!autorizaAdelantarSorteo){ //todos los usuarios listos
                        btnInvitacion.visibility = View.VISIBLE
                    }
                    btnDelete.visibility = View.VISIBLE
                    if(userActual?.temaRegalo.isNullOrBlank()){
                        btnTema.visibility = View.VISIBLE
                    }
                    if(intercambio.organizador == userId){ //Para quitarle ciertos privilegios al usuario común
                        actualIsOwner = true
                        btnEdit.visibility = View.VISIBLE
                        btnDelete.text = getString(R.string.eliminar_intercambio)
                        if(autorizaAdelantarSorteo){
                            btnAdelentar.visibility = View.VISIBLE
                        }
                    }
                }

                // Obtener datos de los participantes
                val tasks = mutableListOf<Task<Pair<Participante, Usuario>>>()

                for (participante in intercambio.participantes) {
                    val task = usersUtils.obtenerUsuarioPorId(participante.uid)
                        .onSuccessTask { usuario ->
                            // Si el task es exitoso, devuelve el par con el participante y el usuario
                            Tasks.forResult(Pair(participante, usuario))
                        }
                        .addOnFailureListener { exception ->
                            // En caso de fallo, agregar un usuario vacío y un participante vacío
                            tasks.add(Tasks.forResult(Pair(participante, Usuario())))
                        }
                    tasks.add(task)
                }

                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener { results ->
                        @Suppress("UNCHECKED_CAST")
                            val participantesInfo = results.mapNotNull { task ->
                                if (task.isSuccessful) task.result as Pair<Participante, Usuario> else null
                            }

                        val miParticipante = participantesInfo.find { (participante, _) -> participante.uid == intercambio.organizador }

                        if (miParticipante != null) {
                            val (participante, usuario) = miParticipante
                            organizador.text = getString(R.string.usuario_info, usuario.nombre, usuario.alias, usuario.email)
                        } else {
                            organizador.text = getString(R.string.no_encontrado)
                        }

                        // Mostrar los participantes en el TextView
                        participantes.text = participantesInfo.joinToString("\n\n") { (participante, usuario) ->
                            val nombreUsuario = usuario.nombre.ifEmpty { getString(R.string.no_registrado) }
                            if (participante.activo) {
                                val (organizadorParticipante, organizadorUsuario) = miParticipante!!
                                if(organizadorParticipante.email == participante.email){
                                    getString(R.string.estatus_participante, nombreUsuario, getString(R.string.organizador), participante.email)
                                }else{
                                    getString(R.string.estatus_participante, nombreUsuario, getString(R.string.activo), participante.email)
                                }
                            } else {
                                getString(R.string.estatus_participante, nombreUsuario, getString(R.string.inv_pendiente), participante.email)
                            }
                        }

                    }
                    .addOnFailureListener { exception ->
                        Log.e("DetalleIntercambio", "Error al obtener participantes: ${exception.message}")
                    }

            }
            .addOnFailureListener {
                // Maneja el error si no se puede obtener el intercambio
                finish()
            }
    }



    private fun enviarInvitacion(enlace: String, nombreIntercambio: String, codigo: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.inv_asunto))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.inv_mensaje, nombreIntercambio, enlace, codigo))
        startActivity(Intent.createChooser(intent, getString(R.string.enviar_invitacion)))
    }

    private fun formatearTemas(temas: List<String>): String {
        return temas.joinToString(separator = ", ") { it.trim() }
    }

}