package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.Clasificacion
import com.example.appconteo.model.ConteoUiState
import com.example.appconteo.model.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConteoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = UsuarioDbHelper(application)

    private fun leerConteo(): ConteoUiState {
        val historialHoy = db.listarHistorial().filter { it.fecha == Fechas.hoy() }
        val clientes = historialHoy.count { it.tipoRegistro == Clasificacion.CLIENTE.etiqueta }
        return ConteoUiState(clientes = clientes, boletasEscaneadas = clientes, historial = historialHoy)
    }

    private val _uiState = MutableStateFlow(leerConteo())
    val uiState: StateFlow<ConteoUiState> = _uiState.asStateFlow()

    fun recargarConteo() {
        val nuevo = leerConteo()
        _uiState.update {
            it.copy(clientes = nuevo.clientes, boletasEscaneadas = nuevo.boletasEscaneadas, historial = nuevo.historial)
        }
    }

    fun intentarEscanear(): Boolean {
        if (_uiState.value.boletasFaltantes <= 0) {
            _uiState.update { it.copy(mensajeError = "⚠️ Ya se escanearon todas las boletas de caja.") }
            return false
        }
        _uiState.update { it.copy(mensajeError = null) }
        return true
    }

    fun validarBoleta(codigo: String): Boolean {
        if (db.existeBoleta(codigo)) {
            _uiState.update { it.copy(mensajeError = "⚠️ Esta boleta ya fue registrada.") }
            return false
        }
        _uiState.update { it.copy(mensajeError = null) }
        return true
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
