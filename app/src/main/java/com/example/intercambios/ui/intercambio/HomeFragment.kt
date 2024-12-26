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
import com.airbnb.lottie.LottieAnimationView
import com.example.intercambios.R
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var intercambioUtils: IntercambioRepository
    private lateinit var animacionBuscar: LottieAnimationView

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
        animacionBuscar = binding.animateSearch
        //Cargar los registros de intercambios hechos
        fetchDataFromFirestore()

    }

    override fun onResume() {
        super.onResume()
        // Limpia el contenedor cada vez que el fragmento se muestra nuevamente
        val container = binding.containerLayout
        container.removeAllViews()
        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val container = binding.containerLayout
        animacionBuscar.visibility = View.GONE
        intercambioUtils.obtenerIntercambios()
            .addOnSuccessListener { intercambios ->
                // Verifica si el fragmento está adjunto antes de actualizar la UI
                if (isAdded) {
                    // Aquí tienes la lista de intercambios
                    if (intercambios.isNotEmpty()) {
                        container.removeAllViews() // Limpia el contenedor antes de agregar nuevas vistas
                        Log.i("Home", intercambios.size.toString())
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
                                textViewHora.text = getString(R.string.hora_intercambio, intercambio.horaIntercambio)
                                textViewParticipantes.text = getString(R.string.cantidad_participantes, (intercambio.personasRegistradas).toString(), intercambio.numPersonas.toString()) // Número de participantes
                                textViewCode.text = getString(R.string.codigo_intercambio, intercambio.code)

                                container.addView(registroView)

                                // Aplica una animación de desvanecimiento
                                registroView.translationY = 200f  // Desplazar la vista hacia abajo inicialmente
                                registroView.alpha = 0f  // Inicialmente invisible
                                registroView.animate()
                                    .translationY(0f)
                                    .alpha(1f)
                                    .setDuration(1000)
                                    .start()

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
                    }else{
                        Log.i("Home", "Sin intercambios")
                        animacionBuscar.visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    animacionBuscar.visibility = View.VISIBLE
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