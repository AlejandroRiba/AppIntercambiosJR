package com.example.intercambios.data.models

data class Usuario(
    val alias: String = "",
    val nombre: String = "",
    val email: String = "",
    val avatar: String = "",
    val verified: Boolean = false
)
