package com.example.appconteo.model

data class RecuperarClaveUiState(
    val correo: String = "",
    val palabraSecreta: String = "",
    val nuevaClave: String = "",
    val repetirClave: String = "",
    val nuevaVisible: Boolean = false,
    val repetirVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorPalabra: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val exito: Boolean = false
)