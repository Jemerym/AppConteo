package com.example.appconteo.model

// CAMBIO: "avatar: ByteArray?" (BLOB en SQLite) pasa a "avatarUrl: String?" (foto en el servidor).
data class Usuario(
    val nombre: String,
    val correo: String,
    val id: Long = 0,
    val rolId: Long = Rol.ID_OBSERVADOR,
    val rolNombre: String = "",
    val contrasena: String = "",       // solo se usa al registrar; la API nunca la devuelve
    val palabraSecreta: String = "",   // solo se usa al registrar; la API nunca la devuelve
    val avatarUrl: String? = null
) {
    val esAdmin: Boolean get() = rolId == Rol.ID_ADMINISTRADOR
}
