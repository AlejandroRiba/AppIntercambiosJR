package com.example.intercambios.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.intercambios.R
import com.example.intercambios.data.models.Users
import com.example.intercambios.ui.HomeActivity
import com.example.intercambios.utils.AvatarAdapter
import com.example.intercambios.utils.GeneralUtils
import kotlin.properties.Delegates

class SelectAvatarActivity : AppCompatActivity() {

    private lateinit var avatarRecyclerView: RecyclerView
    private lateinit var btnGuardar: Button
    private lateinit var btnSaltar: Button
    private var selectedAvatar: Int? = null
    private val usersHelper = Users()
    private val genUtils = GeneralUtils(this)
    private var returnHome by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selectavatar)
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainView.requestApplyInsets()

        avatarRecyclerView = findViewById(R.id.avatarRecyclerView)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnSaltar = findViewById(R.id.btnSaltar)

        val avatarList = listOf(
            R.drawable.avatardef,
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5,
            R.drawable.avatar6,
            R.drawable.avatar7,
            R.drawable.avatar8,
            R.drawable.avatar9,
            R.drawable.avatar10,
            R.drawable.avatar11,
            R.drawable.avatar12,
            R.drawable.avatar13,
            R.drawable.avatar14
        )

        val avatarNames = listOf(
            "avatardef",
            "avatar1",
            "avatar2",
            "avatar3",
            "avatar4",
            "avatar5",
            "avatar6",
            "avatar7",
            "avatar8",
            "avatar9",
            "avatar10",
            "avatar11",
            "avatar12",
            "avatar13",
            "avatar14"
        )

        val avatarAdapter = AvatarAdapter(this, avatarList)
        val avatarSeleccionadoExtra = intent.getStringExtra("avatar") ?: "avatardef"
        returnHome = intent.getBooleanExtra("backHome", true)
        if(!returnHome)
            btnSaltar.text = "Cancelar"
        val index = avatarNames.indexOf(avatarSeleccionadoExtra)
        avatarAdapter.setSelectedPosition(index)

        avatarRecyclerView.layoutManager = GridLayoutManager(this, 3)
        avatarRecyclerView.adapter = avatarAdapter



        btnGuardar.setOnClickListener {
            selectedAvatar = avatarAdapter.getSelectedAvatar()
            if (selectedAvatar != null) {
                val avatarIndex = avatarList.indexOf(selectedAvatar!!)
                usersHelper.updateAvatarImage(avatarNames[avatarIndex])
                backtoHome()
            } else {
                Toast.makeText(this, "Por favor, selecciona un avatar", Toast.LENGTH_SHORT).show()
            }
        }

        btnSaltar.setOnClickListener{
            backtoHome()
        }

    }

    private fun backtoHome(){
        var homeIntent: Intent
        if(returnHome){
            homeIntent = Intent(this, HomeActivity::class.java)
        }else{
            homeIntent = Intent(this, HomeActivity::class.java).apply {
                putExtra("fragment", "perfil")
            }
        }
        finish() //finaliza el seleccionador de avatar
        startActivity(homeIntent)
    }

}