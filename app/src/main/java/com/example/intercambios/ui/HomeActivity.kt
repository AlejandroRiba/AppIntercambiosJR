package com.example.intercambios.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.intercambios.R
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.data.models.Users
import com.example.intercambios.databinding.ActivityHomeBinding
import com.example.intercambios.ui.auth.LoginActivity
import com.example.intercambios.ui.intercambio.CrearIntercambioFragment
import com.example.intercambios.ui.intercambio.HomeFragment
import com.example.intercambios.ui.perfil.PerfilFragment
import com.example.intercambios.utils.NetworkUtils
import com.google.android.material.navigation.NavigationView

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseHelper = AuthUtils(this)

    private val usersUtil =  Users()

    // Flag para asegurar que binding esté inicializado
    private var isBindingInitialized = false
    private var cargaDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (session().isEmpty()){
            val loginIntent = Intent(this, LoginActivity::class.java)
            finish()//finaliza la actividad
            startActivity(loginIntent)//regresa a la pantalla principal
            return
        }

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Inicializamos el binding y configuramos la barra de herramientas
        isBindingInitialized = true
        setSupportActionBar(binding.appBarHome.toolbar)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), false) //Se inicializa con el home fragment
        }

        // Registrar callbacks de ciclo de vida de fragmentos
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                // Mostrar u ocultar el botón flotante según el fragmento actual
                if (f is HomeFragment) {
                    binding.appBarHome.fab.show()
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                } else {
                    binding.appBarHome.fab.hide()
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }
        }, true)

        // Configuración del menú lateral
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView


        val headerView: View = navView.getHeaderView(0)

        usersUtil.obtenerUsuario{ usuario ->
            val correovisible = headerView.findViewById<TextView>(R.id.emailvisible)
            val nombrevisible = headerView.findViewById<TextView>(R.id.nombrevisible)
            if (usuario != null) {
                correovisible.text = usuario.email
                nombrevisible.text = usuario.nombre
            }
        }



        // Configuración del listener para manejar clics manualmente
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Reemplazar con el fragmento "Home"
                    replaceFragment(HomeFragment(), true)
                    true
                }

                R.id.nav_profile -> {
                    // Reemplazar con el fragmento "Profile"¿
                    replaceFragment(PerfilFragment(), true)
                    true
                }

                R.id.nav_settings -> {
                    // Reemplazar con el fragmento "Settings"
                    replaceFragment(PerfilFragment(), true)
                    true
                }

                R.id.logOut -> {
                    // Cerrar sesión y redirigir al Login
                    firebaseHelper.logout()
                    val intent = Intent(this, LoginActivity::class.java)
                    finish()
                    startActivity(intent)
                    true
                }

                else -> {
                    false // Indicar que no se manejó el clic
                }
            }.also {
                // Cerrar el menú lateral después de manejar el clic
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        binding.appBarHome.fab.setOnClickListener {
            // Reemplazar con el fragmento de creación de intercambio
            binding.appBarHome.fab.visibility = View.GONE //Ocultamos el botón de agregar intercambio nuevo
            replaceFragment(CrearIntercambioFragment(), true)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    //Método para ocultar el botón de regreso
    fun configureBackButton(showBackButton: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton) // Muestra/oculta la flecha de regreso
        supportActionBar?.setHomeButtonEnabled(showBackButton) // Activa/desactiva el comportamiento del botón
    }


    //Método para obtener el fragmento actual
    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
    }


    // Método para manejar reemplazos de fragmentos
    private fun replaceFragment(fragment: Fragment, regreso: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment_content_home, fragment)
        if(regreso)
            fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
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
    }

    private fun hideNoConnectionScreen() {
        cargaDialog?.dismiss()
        binding.appBarHome.fab.visibility = View.VISIBLE
        // Habilitar la barra lateral (DrawerLayout)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
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