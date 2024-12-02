package com.example.intercambios.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.NavigationUI
import com.example.intercambios.R
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.databinding.ActivityHomeBinding
import com.example.intercambios.ui.auth.LoginActivity
import com.example.intercambios.utils.NetworkUtils

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private val firebaseHelper = AuthUtils(this)

    // Flag para asegurar que binding esté inicializado
    private var isBindingInitialized = false
    private var cargaDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos el flag después de la creación del binding
        isBindingInitialized = true

        setSupportActionBar(binding.appBarHome.toolbar)

        binding.appBarHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Aqui podemos agregar nuevo intercambio", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Configuración del listener para interceptar clics en el menú
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logOut -> {
                    firebaseHelper.logout()
                    val intent = Intent(this, HomeActivity::class.java) //reinicio de la actividad
                    finish()
                    startActivity(intent)
                    true // Indicar que el clic fue manejado
                }

                else -> {
                    // Dejar que las demás opciones naveguen automáticamente
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    drawerLayout.closeDrawer(GravityCompat.START) // Cerrar el menú lateral
                    true
                }
            }
        }

        if (session().isEmpty()){
            val loginIntent = Intent(this, LoginActivity::class.java)
            finish()//finaliza la actividad
            startActivity(loginIntent)//regresa a la pantalla principal
        }

    }

    override fun onStart() {
        super.onStart()
        if(isBindingInitialized){
            checkNetworkStatus()
        }
    }

    override fun onRestart() {
        super.onRestart()
        if(isBindingInitialized){
            checkNetworkStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        if(isBindingInitialized){
            checkNetworkStatus()
        }
    }

    private fun checkNetworkStatus() {
        if (!NetworkUtils.isConnected(this)) {
            showNoConnectionScreen()
        } else {
            hideNoConnectionScreen()
        }
    }


     private fun showNoConnectionScreen() {
         if (cargaDialog == null) { // Solo mostrar el diálogo si no existe uno
             cargaDialog = mostrarCarga()
         }
         binding.appBarHome.fab.visibility = View.GONE
         // Deshabilitar la barra lateral (DrawerLayout)
         binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
         // Deshabilitar el botón de ajustes
         supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun hideNoConnectionScreen() {
        cargaDialog?.dismiss()
        binding.appBarHome.fab.visibility = View.VISIBLE
        // Habilitar la barra lateral (DrawerLayout)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        // Habilitar el botón de ajustes
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun mostrarCarga() : AlertDialog {
        // Inflar el layout personalizado con Lottie y el texto
        val dialogView = LayoutInflater.from(this).inflate(R.layout.loading_layout, null)

        // Crear el diálogo con el layout inflado
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // No permitirá cerrar tocando fuera del diálogo
            .create()

        // Mostrar el diálogo
        builder.show()

        // Garantizar que se muestre sobre los fragmentos actuales
        val dialogWindow = builder.window
        dialogWindow?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        dialogWindow?.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)

        return builder
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