package com.example.intercambios.ui.intercambio

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

        //Referencias a los componenetes
        btnBack = findViewById(R.id.back)
        btnEdit = findViewById(R.id.edit)
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
        }

        btnBack.setOnClickListener { finish() }
        btnEdit.setOnClickListener { Toast.makeText(this, "Editar intercambio.", Toast.LENGTH_SHORT).show() }

    }

    private fun consultarFirebase(docID: String){
        intercambioUtils.obtenerIntercambioPorId(docID)
            .addOnSuccessListener { intercambio ->
                // Aquí puedes trabajar con el objeto Intercambio
                nombre.text = intercambio.nombre
                fechaIntercambio.text = intercambio.fechaIntercambio
                horaIntercambio.text = intercambio.horaIntercambio
                lugarIntercambio.text = intercambio.lugarIntercambio
                montoIntercambio.text = getString(R.string.monto_intercambio, intercambio.monto)
                code.text = intercambio.code
                fechaMaxRegistro.text = intercambio.fechaMaxRegistro
                numPersonas.text = getString(R.string.num_personas, intercambio.numPersonas)
                descripcion.text = intercambio.descripcion
                val regViewBG = color.background
                try {
                    regViewBG.setTint(Color.parseColor(intercambio.color))
                } catch (e: IllegalArgumentException) {
                    // Si el color no es válido, usa un color por defecto
                    regViewBG.setTint(Color.parseColor("#FFFFFF")) // Blanco por defecto
                }
                temas.text = formatearTemas(intercambio.temas)

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