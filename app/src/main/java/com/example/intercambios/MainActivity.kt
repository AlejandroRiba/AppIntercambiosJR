package com.example.intercambios

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.ui.HomeActivity
import com.example.intercambios.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth



class MainActivity : BaseActivity() {

    private val firebaseHelper = AuthUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun showNoConnectionScreen() {
        setContentView(R.layout.loading_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun hideNoConnectionScreen() {
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



    }

    private fun setup(provider: String){
        val textoView = findViewById<TextView>(R.id.textView)
        textoView.text = provider

        val btnOut = findViewById<Button>(R.id.btnOut)
        btnOut.setOnClickListener {
            /*firebaseHelper.logout()
            val intent = Intent(this, MainActivity::class.java) //reinicio de la actividad
            finish()
            startActivity(intent)*/
            val intent = Intent(this, HomeActivity::class.java) //reinicio de la actividad
            finish()
            startActivity(intent)
        }


    }

    private fun session(): String {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val provider = prefs.getString("provider", null)
        val user = firebaseHelper.getCurrentUser()
        return if(provider != null && user != null){
            user.displayName.toString()
        }else{
            ""
        }
    }
}