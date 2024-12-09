package com.example.intercambios.data.models

// Clase para cada participante
data class Participante(
    val uid: String = "",
    val nombre: String = "",
    val temaRegalo: String = "",
    val asignadoA: String? = null // Puede ser nulo si aún no está asignado
)