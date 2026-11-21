package com.example.appconteo.model

data class Turno(
    val id: Long = 0,
    val usuarioId: Long,
    val observadorNombre: String = "",
    val acceso: Int = 1,
    val horaInicio: String,
    val horaFin: String,
    val fecha: String,
    val anulado: Boolean = false       // anulado: se conserva pero no ocupa el tramo ni cuenta como activo
)