package com.example.intercambios.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.intercambios.R

class ColorSpinnerAdapter(context: Context, private val colors: List<String>) :
    ArrayAdapter<String>(context, 0, colors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createColorView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createColorView(position, convertView, parent)
    }

    private fun createColorView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.spinner_item, parent, false)

        // Configurar la vista del color
        val colorView = view.findViewById<View>(R.id.color_view)

        val color = colors[position]
        colorView.setBackgroundColor(Color.parseColor(color)) // Fondo del rectángulo de color

        return view
    }
}
