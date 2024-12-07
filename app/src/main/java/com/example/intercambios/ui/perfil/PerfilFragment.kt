package com.example.intercambios.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        usersUtil.obtenerUsuario{ usuario ->
            val correovisible = binding.emailTextView
            val nombrevisible = binding.nameTextView
            val aliasvisible = binding.aliasTextView
            val avatar = binding.avatarImageView
            if (usuario != null) {
                val avatarName = usuario.avatar
                // Obtener el identificador del recurso a partir del nombre
                val resId = AvatarResources.getResourceByName(avatarName)
                correovisible.text = usuario.email
                nombrevisible.text = usuario.nombre
                aliasvisible.text = usuario.alias
                avatar.setImageResource(resId)  // Establecer la imagen en el ImageView
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}