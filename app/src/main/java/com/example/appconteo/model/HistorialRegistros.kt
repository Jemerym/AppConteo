package com.example.appconteo.model

data class HistorialRegistros(
    val id: Long = 0,
    val observadorNombre: String,
    val tipoRegistro: String,
    val acceso: Int = 1,
    val idTurno: Long = 0,
    val usuarioId: Long = 0,
    val tipoGrupo: String = "Solo",
    val rangoEtario: String = "25-34",
    val codigoBoleta: String? = null,
    val fecha: String = "",
    val hora: String = ""
) {
    val detalle: String
        get() = "Tipo: $tipoGrupo, Edad: $rangoEtario"
}
