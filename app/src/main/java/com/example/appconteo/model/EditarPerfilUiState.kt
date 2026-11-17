package com.example.appconteo.model

data class EditarPerfilUiState(
    val nombre: String = "",
    val contrasenaActual: String = "",
    val nuevaContrasena: String = "",
    val confirmarNuevaContrasena: String = "",
    val contrasenaActualVisible: Boolean = false,
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarVisible: Boolean = false,
    val errorNombre: String? = null,
    val errorContrasenaActual: String? = null,
    val errorNuevaContrasena: String? = null,
    val errorConfirmar: String? = null
)
