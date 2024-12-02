package com.example.intercambios

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.intercambios.utils.NetworkUtils

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        checkNetworkStatus() // Llamamos aquí, después de que la actividad se haya inicializado
    }

    override fun onResume() {
        super.onResume()
        checkNetworkStatus()
    }

    override fun onRestart() {
        super.onRestart()
        checkNetworkStatus()
    }

    private fun checkNetworkStatus() {
        if (!NetworkUtils.isConnected(this)) {
            showNoConnectionScreen()
        } else {
            hideNoConnectionScreen()
        }
    }

    abstract fun showNoConnectionScreen()

    abstract fun hideNoConnectionScreen()


}
