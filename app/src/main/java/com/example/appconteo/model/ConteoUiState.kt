package com.example.appconteo.model

data class ConteoUiState(
    val totalPersonas: Int = 100,
    val boletasCajaTotal: Int = 15,
    val clientes: Int = 0,
    val boletasEscaneadas: Int = 0,
    val historial: List<HistorialItem> = emptyList()
) {
    val transeuntes: Int
        get() = (totalPersonas - clientes).coerceAtLeast(0)

    val boletasFaltantes: Int
        get() = (boletasCajaTotal - boletasEscaneadas).coerceAtLeast(0)
}