package com.example.appconteo.model

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        const val ID_OBSERVADOR = 2L
    }
}
