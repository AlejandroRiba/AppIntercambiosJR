package com.example.intercambios.utils

import com.example.intercambios.R

object AvatarResources {
    // Mapa de nombres de avatar a recursos
    private val avatarMap = mapOf(
        "avatardef" to R.drawable.avatardef,
        "avatar1" to R.drawable.avatar1,
        "avatar2" to R.drawable.avatar2,
        "avatar3" to R.drawable.avatar3,
        "avatar4" to R.drawable.avatar4,
        "avatar5" to R.drawable.avatar5,
        "avatar6" to R.drawable.avatar6,
        "avatar7" to R.drawable.avatar7,
        "avatar8" to R.drawable.avatar8,
        "avatar9" to R.drawable.avatar9,
        "avatar10" to R.drawable.avatar10,
        "avatar11" to R.drawable.avatar11,
        "avatar12" to R.drawable.avatar12,
        "avatar13" to R.drawable.avatar13,
        "avatar14" to R.drawable.avatar14
    )

    // Función para obtener el recurso por nombre
    fun getResourceByName(name: String): Int {
        return avatarMap[name] ?: R.drawable.avatardef // Si no se encuentra, usa la imagen predeterminada
    }
}
