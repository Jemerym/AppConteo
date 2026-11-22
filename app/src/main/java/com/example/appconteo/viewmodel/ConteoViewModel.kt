package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconteo.data.RegistroRepository
import com.example.appconteo.model.Clasificacion
import com.example.appconteo.model.ConteoUiState
import com.example.appconteo.model.Fechas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConteoViewModel(application: Application) : AndroidViewModel(application) {
    // CAMBIO: los registros ya no vienen de SQLite sino de registros-service.
    private val registroRepo = RegistroRepository()

    private val _uiState = MutableStateFlow(ConteoUiState())
    val uiState: StateFlow<ConteoUiState> = _uiState.asStateFlow()

    init {
        recargarConteo()
    }

    // Los registros de hoy (sin anulados: no cuentan). Si el servidor no responde se deja el último conteo.
    fun recargarConteo() {
        viewModelScope.launch {
            try {
                val historialHoy = registroRepo.listar(fecha = Fechas.hoy())
                val clientes = historialHoy.count { it.tipoRegistro == Clasificacion.CLIENTE.etiqueta }
                _uiState.update {
                    it.copy(clientes = clientes, boletasEscaneadas = clientes, historial = historialHoy)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // En silencio: se conserva el conteo anterior
            }
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

    // Revisa las boletas de los registros vigentes de hoy (ya descargados).
    // Igual, si se repite, el servidor responde "Esta boleta ya fue registrada" al guardar.
    fun validarBoleta(codigo: String): Boolean {
        val yaUsada = _uiState.value.historial.any { it.codigoBoleta == codigo.trim() }
        if (yaUsada) {
            _uiState.update { it.copy(mensajeError = "⚠️ Esta boleta ya fue registrada.") }
            return false
        }
        _uiState.update { it.copy(mensajeError = null) }
        return true
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}
