package com.example.appconteo.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val id: Long = 0,
    val rolId: Long = Rol.ID_OBSERVADOR,
    val rolNombre: String = "",
    val contrasena: String = "",
    val avatar: ByteArray? = null
)