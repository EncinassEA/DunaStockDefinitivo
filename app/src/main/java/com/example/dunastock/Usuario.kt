package com.example.dunastock

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val password_hash: String,
    val rol: String
)