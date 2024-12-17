package com.example.intercambios.ui.intercambio

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.intercambios.R
import com.example.intercambios.data.models.Intercambio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DetalleIntercambio : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_intercambio)

        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val intercambioId = intent.getStringExtra("intercambioId")
        if (intercambioId != null) {
            cargarIntercambio(intercambioId)
        }
    }

    private fun cargarIntercambio(intercambioId: String) {
        db.collection("intercambios").document(intercambioId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val intercambio = document.toObject(Intercambio::class.java)
                    mostrarDetalles(intercambio)

                    // Mostrar el diálogo de confirmación
                    findViewById<Button>(R.id.btnUnirse).setOnClickListener {
                        mostrarDialogoConfirmacion(intercambioId)
                    }
                }
            }
    }

    private fun mostrarDialogoConfirmacion(intercambioId: String) {
        AlertDialog.Builder(this)
            .setTitle("Aceptar Invitación")
            .setMessage("¿Deseas unirte a este intercambio?")
            .setPositiveButton("Sí") { _, _ ->
                agregarParticipante(intercambioId) // Agregar al participante
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun agregarParticipante(intercambioId: String) {
        val participante = mapOf(
            "uid" to userId
        )

        db.collection("intercambios").document(intercambioId)
            .update("participantes", FieldValue.arrayUnion(participante))
            .addOnSuccessListener {
                Toast.makeText(this, "Te has unido al intercambio", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al unirse al intercambio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDetalles(intercambio: Intercambio?) {
        val nombreTextView = findViewById<TextView>(R.id.tvNombreIntercambio)
        val descripcionTextView = findViewById<TextView>(R.id.tvDescripcionIntercambio)
        val participantesTextView = findViewById<TextView>(R.id.tvParticipantesLabel)

        intercambio?.let {
            nombreTextView.text = it.nombre ?: "Sin nombre"
            descripcionTextView.text = it.descripcion ?: "Sin descripción"

            // Mostrar el número de participantes
            val totalParticipantes = it.participantes?.size ?: 0
            participantesTextView.text = "Participantes: $totalParticipantes"
        }
    }


}
