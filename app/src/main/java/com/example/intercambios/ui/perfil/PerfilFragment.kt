package com.example.intercambios.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.intercambios.data.models.Users
import com.example.intercambios.databinding.FragmentPerfilBinding
import com.example.intercambios.utils.AvatarResources

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val usersUtil =  Users()

    private lateinit var correo: String
    private lateinit var nombre: String
    private lateinit var descript: String
    private lateinit var avatarName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        correo = ""
        nombre = ""
        descript = ""
        avatarName = ""

        usersUtil.obtenerUsuario{ usuario ->
            val correovisible = binding.emailTextView
            val nombrevisible = binding.nameTextView
            val aliasvisible = binding.aliasTextView
            val descripcionvisible = binding.descriptionContent
            val avatar = binding.avatarImageView
            if (usuario != null) {
                correo = usuario.email
                nombre = usuario.nombre
                descript = usuario.descripcion
                avatarName = usuario.avatar
                // Obtener el identificador del recurso a partir del nombre
                val resId = AvatarResources.getResourceByName(avatarName)
                correovisible.text = correo
                nombrevisible.text = nombre
                aliasvisible.text = usuario.alias
                if(descript.isEmpty()){
                    descripcionvisible.text = "Sin descripción."
                }else{
                    descripcionvisible.text = descript
                }
                avatar.setImageResource(resId)  // Establecer la imagen en el ImageView
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editAvatarFab.setOnClickListener{
            val avatarIntent = Intent(context, SelectAvatarActivity::class.java).apply {
                putExtra("avatar", avatarName)
                putExtra("backHome", false)
            }
            startActivity(avatarIntent)
        }
        binding.editDataFab.setOnClickListener{
            Toast.makeText(requireActivity(), "Editar datos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}