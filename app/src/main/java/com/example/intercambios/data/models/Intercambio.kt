package com.example.intercambios.data.models

data class Intercambio(
    val code: String = "",
    val nombre: String = "",
    val numPersonas: Int = 0,
    val descripcion: String = "",
    val fechaMaxRegistro: String = "",
    val fechaIntercambio: String = "",
    val horaIntercambio: String = "",
    val lugarIntercambio: String = "",
    val color: String = "",
    val personasRegistradas: Int = 0,
    val participantes: List<Participante> = emptyList(), // Lista de participantes
    val temas: List<String> = emptyList()
)