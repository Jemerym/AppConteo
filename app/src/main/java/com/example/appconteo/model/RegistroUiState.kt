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
    val errorTerminos: String? = null,

    val registroExitoso: Boolean = false
) {
    val tieneMinimo8: Boolean get() = contrasena.length >= 8
    val tieneMayuscula: Boolean get() = contrasena.any { it.isUpperCase() }
    val tieneNumero: Boolean get() = contrasena.any { it.isDigit() }
    val tieneSimbolo: Boolean get() = contrasena.any { !it.isLetterOrDigit() }
    val contrasenaValida: Boolean get() = tieneMinimo8 && tieneMayuscula && tieneNumero && tieneSimbolo
}
