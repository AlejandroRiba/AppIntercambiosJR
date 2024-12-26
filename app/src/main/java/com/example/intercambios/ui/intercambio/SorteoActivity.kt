package com.example.intercambios.ui.intercambio

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.intercambios.R
import com.example.intercambios.data.models.IntercambioRepository
import com.example.intercambios.data.models.Participante
import com.example.intercambios.utils.GeneralUtils

class SorteoActivity : AppCompatActivity() {

    private lateinit var btnListo: Button
    private lateinit var animacion: LottieAnimationView
    private lateinit var titulo: TextView

    private lateinit var docId: String

    private lateinit var listaParticipantes: List<Participante>

    private  var intercambioUtils = IntercambioRepository()
    private val genutils = GeneralUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sorteo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        docId = intent.getStringExtra("docId") ?: ""
        if(docId.isBlank()){
            genutils.showAlertandFinish(getString(R.string.no_encontrado_email),getString(R.string.error_title))
        }else{
            btnListo = findViewById(R.id.listo)
            titulo = findViewById(R.id.TitleTextView)
            animacion = findViewById(R.id.animateSort)
            //btnListo.visibility = View.INVISIBLE
            btnListo.setOnClickListener {
                finish()
            }
            sorteo()
        }

    }

    private fun sorteo(){
        intercambioUtils.obtenerIntercambioPorId(docId)
            .addOnSuccessListener { intercambio ->
                if(intercambio.sorteo){
                    genutils.showAlertandFinish(getString(R.string.sorteo_listo),getString(R.string.error_title))
                }else{
                    listaParticipantes = intercambio.participantes
                    Log.i("SorteoAct", listaParticipantes.toString())
                    Handler(Looper.getMainLooper()).postDelayed({
                        animacion.setAnimation(R.raw.success) // Cambia el recurso de Lottie
                        btnListo.visibility = View.VISIBLE // Muestra el botón
                        titulo.text = getString(R.string.resultados_listos)
                    }, 6000) // Tiempo en milisegundos (6 segundos)
                }
            }.addOnFailureListener{
                genutils.showAlertandFinish(getString(R.string.no_encontrado_email),getString(R.string.error_title))
            }
    }

}