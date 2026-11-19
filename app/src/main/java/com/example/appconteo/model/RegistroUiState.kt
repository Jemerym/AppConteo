package com.example.appconteo.model

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val palabraSecreta: String = "",
    val aceptaTerminos: Boolean = false,

    val contrasenaVisible: Boolean = false,

    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorPalabraSecreta: String? = null,
    val errorTerminos: String? = null
)