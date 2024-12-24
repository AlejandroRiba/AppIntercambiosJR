package com.example.intercambios.ui.intercambio

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.intercambios.R
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.data.models.Usuario
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth

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
    private lateinit var personasRegistradas: TextView
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
    private var autorizaSalir: Boolean = true
    private var sorteoRealizado = false
    private var autorizaAdelantarSorteo = false
    private var unirNuevoUser: Boolean = false

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

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

        intercambioUtils = IntercambioRepository()
        val docID = intent.getStringExtra("docId") ?: ""
        val codigo = intent.getStringExtra("codigo") ?: ""
        unirNuevoUser = intent.getBooleanExtra("union", false)

        //Referencias a los componenetes
        btnBack = findViewById(R.id.back)
        btnEdit = findViewById(R.id.edit)
        btnAdelentar = findViewById(R.id.btn_adelantar)
        btnDelete = findViewById(R.id.btn_delete)
        btnUnirme = findViewById(R.id.btn_unirme)
        btnTema = findViewById(R.id.btn_selecciontema)
        btnConsultaSort = findViewById(R.id.btn_consultarsorteo)
        btnUnirme.visibility = View.GONE //INICIALMENTE OCULTOS
        btnAdelentar.visibility = View.GONE
        btnEdit.visibility = View.GONE
        btnTema.visibility = View.GONE
        btnDelete.visibility = View.GONE
        btnConsultaSort.visibility = View.GONE
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

        if(docID.isNotEmpty()) {
            Log.i("DetalleIntercambio", docID)
            consultarFirebase(docID)
        }else{
            if(codigo.isNotBlank()){
                intercambioUtils.obtenerDocId(codigo)
                    .addOnSuccessListener { docID ->
                        // Si se encuentra el documento, llama a consultarFirebase
                        consultarFirebase(docID)
                    }
                    .addOnFailureListener { exception ->
                        // Si ocurre un error, muestra un AlertDialog y finaliza la Activity
                        AlertDialog.Builder(this)
                            .setTitle("Intercambio no encontrado")
                            .setMessage("No se encontró ningún intercambio con el código proporcionado.")
                            .setPositiveButton("Aceptar") { _, _ ->
                                finish() // Finaliza la Activity al cerrar el AlertDialog
                            }
                            .setCancelable(false) // Evita que el usuario lo cierre fuera del botón
                            .show()
                    }
            }else{
                finish() //si el codigo esta vacio y no hay docID entonces bye
            }
        }

        btnBack.setOnClickListener { finish() }
        btnEdit.setOnClickListener { Toast.makeText(this, "Editar intercambio.", Toast.LENGTH_SHORT).show() }

        btnDelete.setOnClickListener {
            if(autorizaSalir){
                Toast.makeText(this, "Eliminar/salir del intercambio.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, getString(R.string.warning_salir), Toast.LENGTH_SHORT).show()
            }
        }

        btnConsultaSort.setOnClickListener {
            if(sorteoRealizado){
                Toast.makeText(this, "Muestra la pantalla liberando a quien tienes asignado.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, getString(R.string.warning_sorteo), Toast.LENGTH_SHORT).show()
            }
        }

        btnAdelentar.setOnClickListener { Toast.makeText(this, "Adelantar sorteo", Toast.LENGTH_SHORT).show() }
        btnUnirme.setOnClickListener { Toast.makeText(this, "Unirme al intercambio", Toast.LENGTH_SHORT).show() }
    }

    private fun consultarFirebase(docID: String){
        intercambioUtils.obtenerIntercambioPorId(docID)
            .addOnSuccessListener { intercambio ->
                nombre.text = intercambio.nombre
                fechaIntercambio.text = intercambio.fechaIntercambio
                horaIntercambio.text = intercambio.horaIntercambio
                lugarIntercambio.text = intercambio.lugarIntercambio
                montoIntercambio.text = getString(R.string.monto_intercambio, intercambio.monto)
                code.text = intercambio.code
                fechaMaxRegistro.text = intercambio.fechaMaxRegistro
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
                if(todosActivos == null){ //si no encuentra ninguno inactivo puede adelantar el sorteo
                    autorizaAdelantarSorteo = true
                }

                val userActual =  intercambio.participantes.find { participante -> participante.uid == userId }
                if(userActual == null && unirNuevoUser){ //se debe especificar en el intent
                    btnUnirme.visibility = View.VISIBLE //MOSTRAMOS EL BOTÓN PARA UNIRSE
                }else{ //EN caso de que si lo encuentre
                    // Aquí puedes trabajar con el objeto Intercambio
                    btnConsultaSort.visibility = View.VISIBLE
                    btnDelete.visibility = View.VISIBLE
                    btnTema.visibility = View.VISIBLE
                    if(intercambio.organizador == userId){ //Para quitarle ciertos privilegios al usuario común
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
            .addOnFailureListener { exception ->
                // Maneja el error si no se puede obtener el intercambio
                finish()
            }
    }

    private fun formatearTemas(temas: List<String>): String {
        return temas.joinToString(separator = ", ") { it.trim() }
    }

}