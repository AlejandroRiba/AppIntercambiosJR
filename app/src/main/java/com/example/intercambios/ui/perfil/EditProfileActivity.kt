package com.example.intercambios.ui.perfil


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.intercambios.data.models.UsersRepository
import com.example.intercambios.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val usersUtil = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Rellenar los campos con los datos del usuario
        val alias = intent.getStringExtra("alias") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""

        binding.editAlias.setText(alias)
        binding.editNombre.setText(nombre)
        binding.editDescripcion.setText(descripcion)

        // Manejar el botón de guardar
        binding.saveButton.setOnClickListener {
            val updatedData = mapOf(
                "alias" to binding.editAlias.text.toString(),
                "nombre" to binding.editNombre.text.toString(),
                "descripcion" to binding.editDescripcion.text.toString()
            )
            usersUtil.actualizarUsuario(updatedData) { success ->
                if (success) {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}