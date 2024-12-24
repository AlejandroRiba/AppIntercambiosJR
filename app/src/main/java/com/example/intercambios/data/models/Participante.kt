package com.example.intercambios.data.models

data class Participante(
    val uid: String = "",
    val email: String = "",
    val temaRegalo: String = "",
    val asignadoA: String? = null,
    val activo: Boolean = false, //false pendiente, true activo
)