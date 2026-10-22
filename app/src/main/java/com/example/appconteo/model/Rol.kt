package com.example.appconteo.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        // NUEVO (Rol): id del rol que se asigna al registrarse desde el Formulario.
        // Coincide con el dato precargado en onCreate (3 = Estudiante).
        const val ID_ESTUDIANTE = 3L
    }
}
