package com.example.appconteo.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_ADMINISTRADOR = 1L
        const val ID_OBSERVADOR = 2L
    }
}

enum class RolEnum(val id: Long, val nombre: String) {
    ADMINISTRADOR(1L, "Administrador"),
    OBSERVADOR(2L, "Observador");

}
