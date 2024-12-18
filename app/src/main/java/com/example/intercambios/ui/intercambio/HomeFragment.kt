package com.example.intercambios.ui.intercambio

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.intercambios.R
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var intercambioUtils: IntercambioRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intercambioUtils = IntercambioRepository()
        //Cargar los registros de intercambios hechos
        fetchDataFromFirestore()

    }

    private fun fetchDataFromFirestore() {
        val container = binding.containerLayout
        intercambioUtils.obtenerIntercambios()
            .addOnSuccessListener { intercambios ->
                // Verifica si el fragmento está adjunto antes de actualizar la UI
                if (isAdded) {
                    // Aquí tienes la lista de intercambios
                    if (intercambios.isNotEmpty()) {
                        container.removeAllViews() // Limpia el contenedor antes de agregar nuevas vistas

                        for ((intercambio, documentId) in intercambios) {
                            try {
                                val registroView = layoutInflater.inflate(R.layout.item_registro, container, false)
                                val regViewContainer = registroView.findViewById<LinearLayout>(R.id.contenedorIntercambio)
                                val regViewBG = regViewContainer.background
                                try {
                                    regViewBG.setTint(Color.parseColor(intercambio.color))
                                } catch (e: IllegalArgumentException) {
                                    // Si el color no es válido, usa un color por defecto
                                    regViewBG.setTint(Color.parseColor("#FFFFFF")) // Blanco por defecto
                                }
                                val textViewNombre = registroView.findViewById<TextView>(R.id.textViewNombre)
                                val textViewFecha = registroView.findViewById<TextView>(R.id.textViewFecha)
                                val textViewHora = registroView.findViewById<TextView>(R.id.textViewHora)
                                val textViewParticipantes = registroView.findViewById<TextView>(R.id.textViewParticipantes)
                                val textViewCode = registroView.findViewById<TextView>(R.id.textViewCode)

                                textViewNombre.text = intercambio.nombre
                                textViewFecha.text = dateFormatting(intercambio.fechaIntercambio)
                                textViewHora.text = "Hora: ${intercambio.horaIntercambio}"
                                textViewParticipantes.text = "Participantes: ${(intercambio.participantes).size} / ${intercambio.numPersonas}" // Número de participantes
                                textViewCode.text = "Código: ${intercambio.code}"

                                container.addView(registroView)

                                regViewContainer.setOnClickListener {
                                    val intent = Intent(requireActivity(), DetalleIntercambio::class.java).apply {
                                        putExtra("docId", documentId)
                                    }
                                    //requireActivity().finish()
                                    startActivity(intent)
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireActivity(), "Error al procesar el intercambio.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireActivity(), "No se encontraron intercambios.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireActivity(), "Error al obtener intercambios.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Cambia el formato de las fechas
    fun dateFormatting(date: String): String {
        var meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

        var anio = date.substring(0, 4)
        var mes = meses[(date.substring(5, 7).toInt()) - 1]
        var dia = date.substring(8)

        return "$dia - $mes - $anio"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}