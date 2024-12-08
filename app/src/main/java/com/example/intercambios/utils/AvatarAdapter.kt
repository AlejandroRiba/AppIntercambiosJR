package com.example.intercambios.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.intercambios.R

class AvatarAdapter(
    private val context: Context,
    private val avatarList: List<Int>, // Lista de recursos de imágenes
    private var selectedPosition: Int = -1 // Para rastrear el avatar seleccionado
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatarResId = avatarList[position]
        holder.avatarImageView.setImageResource(avatarResId)

        // Resaltar el avatar seleccionado
        if (position == selectedPosition) {
            holder.avatarImageView.alpha = 1.0f // Avatar seleccionado
        } else {
            holder.avatarImageView.alpha = 0.6f // Avatares no seleccionados
        }

        // Manejar selección del avatar
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            // Notificar cambios en los elementos previos y actuales
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount(): Int = avatarList.size

    // Obtener el recurso del avatar seleccionado
    fun getSelectedAvatar(): Int? {
        return if (selectedPosition != -1) avatarList[selectedPosition] else null
    }

    class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
    }
}
