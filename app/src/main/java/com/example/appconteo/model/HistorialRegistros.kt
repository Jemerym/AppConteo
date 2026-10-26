package com.example.appconteo.model

data class HistorialRegistros(
    val id: Long = 0,
    val observadorNombre: String,
    val tipoRegistro: String,
    val detalle: String,
    val hora: String
)