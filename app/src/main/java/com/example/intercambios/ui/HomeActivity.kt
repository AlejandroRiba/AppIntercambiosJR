package com.example.intercambios.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.intercambios.R
import com.example.intercambios.data.firebase.AuthUtils
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.databinding.ActivityHomeBinding
import com.example.intercambios.ui.auth.LoginActivity
import com.example.intercambios.ui.intercambio.CrearIntercambioActivity
import com.example.intercambios.ui.intercambio.DetalleIntercambio
import com.example.intercambios.ui.intercambio.HomeFragment
import com.example.intercambios.ui.perfil.PerfilFragment
import com.example.intercambios.ui.perfil.SettingFragment
import com.example.intercambios.utils.AvatarResources
import com.example.intercambios.utils.LoadingFragment
import com.example.intercambios.utils.NetworkUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

enum class ProviderType{
    BASIC,
    GOOGLE
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val firebaseHelper = AuthUtils(this)

    private val usersUtil =  UsersRepository()

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

        //Escuchador del link
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val idIntercambio = deepLink.getQueryParameter("id")
                    if (idIntercambio != null) {
                        abrirDetalleIntercambio(idIntercambio)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("DynamicLink", "Error al obtener enlace dinámico", e)
            }

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

        /*// Callback para manejar el evento de retroceso
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)

                if (currentFragment is HomeFragment || currentFragment is LoadingFragment) {
                    showExitConfirmationDialog() // Mostrar diálogo para salir
                } else {
                    // Realiza el "back" normal
                    supportFragmentManager.popBackStack()
                    Log.i("BACKBUTTON", "buttonpressed")

                    // Actualiza UI según el fragmento actual
                    val updatedFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
                    when (updatedFragment) {
                        is HomeFragment -> {
                            binding.appBarHome.fab.visibility = View.VISIBLE
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
        }

        // Registrar el callback en el dispatcher
        onBackPressedDispatcher.addCallback(this, callback)*/

        // Configuración del listener para manejar clics manualmente
        navView.setNavigationItemSelectedListener { menuItem ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Reemplazar con el fragmento "Home"
                    if (currentFragment !is HomeFragment){
                        clearBackStack()
                        replaceFragment(HomeFragment(), false)
                        binding.appBarHome.fab.visibility = View.VISIBLE
                        navView.setCheckedItem(R.id.nav_home) // Marcar el item de "Home"
                        supportActionBar?.title = getString(R.string.menu_home)
                        true
                    }else{
                        false
                    }
                }

                R.id.nav_profile -> {
                    // Reemplazar con el fragmento "Profile"¿
                    if(currentFragment !is PerfilFragment){
                        clearBackStack()
                        binding.appBarHome.fab.visibility = View.GONE //Ocultamos el botón de agregar intercambio nuevo
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
                    if(currentFragment !is SettingFragment){
                        clearBackStack()
                        binding.appBarHome.fab.visibility = View.GONE //Ocultamos el botón de agregar intercambio nuevo
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
            // Crear el AlertDialog
            mostrarDialogoOpciones()
        }

    }

    private fun abrirDetalleIntercambio(codigoIntercambio: String) {
        val intent = Intent(this, DetalleIntercambio::class.java).apply {
            putExtra("codigo", codigoIntercambio)
            putExtra("union", true)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoOpciones() {
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_newexch, null)

        val btnNuevoIntercambio = dialogView.findViewById<Button>(R.id.btnNuevoIntercambio)
        val btnIngresarCodigo = dialogView.findViewById<Button>(R.id.btnIngresarCodigo)

        val opcionesDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Acción para "Nuevo Intercambio"
        btnNuevoIntercambio.setOnClickListener {
            val newIntercambioIntent = Intent(this, CrearIntercambioActivity::class.java)
            startActivity(newIntercambioIntent)
            opcionesDialog.dismiss()
        }

        // Acción para "Ingresar Código"
        btnIngresarCodigo.setOnClickListener {
            mostrarDialogoCodigo()
            opcionesDialog.dismiss()
        }

        opcionesDialog.show()
    }

    private fun mostrarDialogoCodigo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ingresar_codigo, null)
        val codigoInput = dialogView.findViewById<EditText>(R.id.editTextCodigo)

        val codigoDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnEnviarCodigo).setOnClickListener {
            val codigo = codigoInput.text.toString()
            if (codigo.isNotBlank()) {
                val detalleIntercambioIntent = Intent(this, DetalleIntercambio::class.java).apply {
                    putExtra("codigo", codigo)
                    putExtra("union", true)
                }
                startActivity(detalleIntercambioIntent)
                codigoDialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor ingresa un código válido", Toast.LENGTH_SHORT).show()
            }
        }

        codigoDialog.show()
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
                    binding.appBarHome.fab.visibility = View.VISIBLE
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
                    binding.appBarHome.fab.visibility = View.VISIBLE
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
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_home)
        if(currentFragment !is HomeFragment){
            clearBackStack()
            replaceFragment(HomeFragment(), false)
            supportActionBar?.title = getString(R.string.menu_home)
            binding.navView.setCheckedItem(R.id.nav_home)
            binding.appBarHome.fab.visibility = View.VISIBLE
        }
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)

        val exitDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            finish()
        }

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            exitDialog.dismiss()
        }

        exitDialog.show()
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