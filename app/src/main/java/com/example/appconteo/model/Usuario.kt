package com.example.appconteo.model

class Usuario(
    val nombre: String,
    val correo: String,
    val id: Long = 0,
    val rolId: Long = Rol.ID_ESTUDIANTE,
    val rolNombre: String = "",
    val contrasena: String = ""
)