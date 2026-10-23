package com.example.appconteo.model

data class FormularioUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaTerminos: Boolean = false,

    val contrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,

    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val errorTerminos: String? = null,

    val registroExitoso: Boolean = false
)