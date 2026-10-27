package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.ConteoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConteoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConteoUiState())
    val uiState: StateFlow<ConteoUiState> = _uiState.asStateFlow()

    fun escanearBoleta() {
        val estado = _uiState.value
        if (estado.boletasFaltantes <= 0) return

        _uiState.update {
            it.copy(
                clientes = it.clientes + 1,
                boletasEscaneadas = it.boletasEscaneadas + 1
            )
        }
    }

    fun actualizarClientes(clientes: Int) {
        _uiState.update {
            it.copy(
                clientes = clientes,
                boletasEscaneadas = clientes
            )
        }
    }

    fun Limpiar() {
            _uiState.value = ConteoUiState()
        }
}