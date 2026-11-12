package com.example.appconteo.model

data class ProgramarUiState(
    val accesoSeleccionado: Int = 1,
    val observadorSeleccionado: Usuario? = null,
    val diasOffset: Int = 0,
    val tramoSeleccionado: Pair<String, String> = "10:00" to "10:30",
    val mensajeError: String? = null,
    val asignacionExitosa: Boolean = false
)
