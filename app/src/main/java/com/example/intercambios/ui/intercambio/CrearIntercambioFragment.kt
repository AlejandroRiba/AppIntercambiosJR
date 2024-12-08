package com.example.intercambios.ui.intercambio

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.intercambios.R
import com.example.intercambios.databinding.NuevoIntercambioBinding
import com.google.firebase.Timestamp
import java.util.Calendar
import com.example.intercambios.utils.ColorSpinnerAdapter
import com.example.intercambios.utils.TimePickerFragment

class CrearIntercambioFragment : Fragment() {
    private var _binding: NuevoIntercambioBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var spinnerColor: Spinner
    private lateinit var selectedcolor: String

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
        val nombre = binding.edTextNombre
        val personas = binding.edTextNumPersonas
        val descripcion = binding.exchangeDescriptionEditText
        val montoMax = binding.edTextMontoMax
        val fechaRegistro = binding.edTextFechaRegistro
        val fechaIntercambio = binding.edTextFechaIntercambio
        val horaIntercambio = binding.edTextHora
        val lugarIntercambio = binding.edTextLugar
        spinnerColor = binding.colorSpinner

        //asigna las opciones al spinner
        inicializaSpinners()

        horaIntercambio.setOnClickListener { showTimePickerDialog(horaIntercambio) }

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


}