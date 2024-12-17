package com.example.intercambios.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
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
import com.example.intercambios.ui.perfil.SettingFragment
import com.example.intercambios.utils.AvatarResources
import com.example.intercambios.utils.LoadingFragment
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

        // Configuración del menú lateral
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Configuración del ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarHome.toolbar, // El Toolbar que estás utilizando
            R.string.navigation_drawer_open, // Texto para accesibilidad (defínelo en strings.xml)
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), false) //Se inicializa con el home fragment
            navView.setCheckedItem(R.id.nav_home) // Marcar el item de "Profile"
            supportActionBar?.title = getString(R.string.menu_home)
        }

        /*// Registrar callbacks de ciclo de vida de fragmentos
        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)

            }
        }, true)*/


        // Configuración del listener para manejar clics manualmente
        navView.setNavigationItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Reemplazar con el fragmento "Home"
                    if (currentFragment !is HomeFragment){
                        clearBackStack()
                        replaceFragment(HomeFragment(), false)
                        navView.setCheckedItem(R.id.nav_home) // Marcar el item de "Home"
                        supportActionBar?.title = getString(R.string.menu_home)
                        true
                    }else{
                        false
                    }
                }

                R.id.nav_profile -> {
                    // Reemplazar con el fragmento "Profile"¿
                    if(currentFragment !is PerfilFragment && currentFragment !is CrearIntercambioFragment){
                        clearBackStack()
                        replaceFragment(PerfilFragment(), true)
                        navView.setCheckedItem(R.id.nav_profile) // Marcar el item de "Profile"
                        supportActionBar?.title = getString(R.string.perfil)
                        true
                    }else{
                        false
                    }
                }

                R.id.nav_settings -> {
                    // Reemplazar con el fragmento "Settings"
                    if(currentFragment !is SettingFragment && currentFragment !is CrearIntercambioFragment){
                        clearBackStack()
                        replaceFragment(SettingFragment(), true)
                        navView.setCheckedItem(R.id.nav_settings) // Marcar el item de "Profile"
                        supportActionBar?.title = getString(R.string.ajustes)
                        true
                    }else{
                        false
                    }

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
            val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
            if(currentFragment is HomeFragment){
                binding.appBarHome.fab.visibility = View.GONE //Ocultamos el botón de agregar intercambio nuevo
                supportActionBar?.title = getString(R.string.nuevo_intercambio)
                replaceFragment(CrearIntercambioFragment(), true)
            }
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        var currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
        if(currentFragment is HomeFragment || currentFragment is LoadingFragment){
            showExitConfirmationDialog()
            return false
        }else{
            onBackPressedDispatcher.onBackPressed()
            Log.i("BACKBUTTON", "buttonpressedNAV")
            // Comprobar si el fragmento actual es el HomeFragment y actualizar el ítem seleccionado
            currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
            when (currentFragment) {
                is HomeFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_home)
                    supportActionBar?.title = getString(R.string.menu_home)
                }

                is PerfilFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_profile)
                    supportActionBar?.title = getString(R.string.perfil)
                }

                is SettingFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_settings)
                    supportActionBar?.title = getString(R.string.ajustes)
                }
            }
            return true
        }
    }


    //Metodo despreciado para Android 13+, para inferiores es necesario
    override fun onBackPressed() {
        var currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
        if(currentFragment is HomeFragment || currentFragment is LoadingFragment){
            showExitConfirmationDialog()
        }else{
            super.onBackPressed() //SI la actual no es home, entonces hace el back
            Log.i("BACKBUTTON", "buttonpressed")
            currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
            when (currentFragment) {
                is HomeFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_home)
                    supportActionBar?.title = getString(R.string.menu_home)
                }

                is PerfilFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_profile)
                    supportActionBar?.title = getString(R.string.perfil)
                }

                is SettingFragment -> {
                    binding.navView.setCheckedItem(R.id.nav_settings)
                    supportActionBar?.title = getString(R.string.ajustes)
                }
            }
        }
    }


    private fun clearBackStack() {
        // Eliminar todos los fragmentos del back stack
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // Método para manejar reemplazos de fragmentos
    private fun replaceFragment(fragment: Fragment, regreso: Boolean) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment_content_home, fragment)
            if (regreso) {
                fragmentTransaction.addToBackStack(null)
            }
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()  // Para depurar y encontrar el error
        }
    }



    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (isBindingInitialized) {
                checkNetworkStatus()
            }
        }, 200)
    }

    override fun onRestart() {
        super.onRestart()
        Handler(Looper.getMainLooper()).postDelayed({
            if (isBindingInitialized) {
                checkNetworkStatus()
            }
        }, 200)
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            if (isBindingInitialized) {
                checkNetworkStatus()
            }
        }, 200)
    }

    private fun checkNetworkStatus() {
        Log.d("FragmentTransaction", "Revisando estado de red.")
        if (!NetworkUtils.isConnected(this)) {
            showNoConnectionScreen()
        } else {
            hideNoConnectionScreen()
        }
    }


    private fun showNoConnectionScreen() {
         clearBackStack()
         replaceFragment(LoadingFragment(), false)
         supportActionBar?.title = getString(R.string.espera_seg)
         binding.appBarHome.fab.visibility = View.GONE
         // Deshabilitar la barra lateral (DrawerLayout)
         binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun hideNoConnectionScreen() {
        clearBackStack()
        replaceFragment(HomeFragment(), false)
        supportActionBar?.title = getString(R.string.menu_home)
        binding.navView.setCheckedItem(R.id.nav_home)
        actualizarHeaderPerfil()
        // Habilitar la barra lateral (DrawerLayout)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun actualizarHeaderPerfil(){
        val navView: NavigationView = binding.navView
        val headerView: View = navView.getHeaderView(0)

        usersUtil.obtenerUsuario{ usuario ->
            val correovisible = headerView.findViewById<TextView>(R.id.emailvisible)
            val nombrevisible = headerView.findViewById<TextView>(R.id.nombrevisible)
            val avatar = headerView.findViewById<ImageView>(R.id.imageView)
            if (usuario != null) {
                val avatarName = usuario.avatar
                // Obtener el identificador del recurso a partir del nombre
                val resId = AvatarResources.getResourceByName(avatarName)
                correovisible.text = usuario.email
                nombrevisible.text = usuario.nombre
                avatar.setImageResource(resId)  // Establecer la imagen en el ImageView
            }
        }
    }

    private fun showExitConfirmationDialog() {
        // Crear un AlertDialog con opciones de confirmación
        AlertDialog.Builder(this)
            .setMessage("¿Estás seguro de que quieres salir de la aplicación?")
            .setCancelable(false) // Impide que el usuario cierre el diálogo tocando fuera de él
            .setPositiveButton("Sí") { _, _ ->
                // Si el usuario confirma, salir de la aplicación
                finish() // Cierra la actividad, lo que cerrará la aplicación
            }
            .setNegativeButton("No") { dialog, _ ->
                // Si el usuario cancela, simplemente cerrar el diálogo
                dialog.dismiss()
            }
            .create()
            .show()
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