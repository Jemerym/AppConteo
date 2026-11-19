package com.example.appconteo.model

/** Lo que el usuario quiere cambiar en "Modificar perfil". La contraseña actual la verifica el servidor. */
data class CambiosPerfil(
    val nombre: String,
    val contrasenaActual: String,
    val nuevaContrasena: String? = null
)
