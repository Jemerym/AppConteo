package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.ProgramarUiState
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProgramarViewModel : ViewModel() {
    private var turnoIdEditando: Long = 0L

    private val _uiState = MutableStateFlow(ProgramarUiState())
    val uiState: StateFlow<ProgramarUiState> = _uiState.asStateFlow()

    fun cargarTurno(turnoId: Long, buscarTurno: (Long) -> Turno?, buscarUsuario: (Long) -> Usuario?) {
        turnoIdEditando = turnoId
        if (turnoId > 0L) {
            val turno = buscarTurno(turnoId)
            if (turno != null) {
                val observador = buscarUsuario(turno.usuarioId)
                _uiState.update {
                    it.copy(
                        accesoSeleccionado = turno.acceso,
                        observadorSeleccionado = observador,
                        diasOffset = if (turno.fecha == Fechas.conOffset(1)) 1 else 0,
                        tramoSeleccionado = turno.horaInicio to turno.horaFin,
                        esEdicion = true
                    )
                }
            }
        }
    }

    fun onAccesoSeleccionado(acceso: Int) {
        _uiState.update { it.copy(accesoSeleccionado = acceso, mensajeError = null) }
    }

    fun onObservadorSeleccionado(observador: Usuario?) {
        _uiState.update { it.copy(observadorSeleccionado = observador, mensajeError = null) }
    }

    fun onDiasOffsetCambia(offset: Int) {
        _uiState.update { it.copy(diasOffset = offset, mensajeError = null) }
    }

    fun onTramoSeleccionado(tramo: Pair<String, String>) {
        _uiState.update { it.copy(tramoSeleccionado = tramo, mensajeError = null) }
    }

    fun programarTurno(
        verificarSolapamiento: (Long, Int, String, String, String, Long) -> Boolean,
        actualizar: (Long, Long, Int, String, String, String) -> Int,
        insertar: (Long, Int, String, String, String) -> Long
    ): Boolean {
        val estado = _uiState.value
        val obs = estado.observadorSeleccionado ?: run {
            _uiState.update { it.copy(mensajeError = "⚠️ Selecciona un observador.") }
            return false
        }

        val fechaCalculada = Fechas.conOffset(estado.diasOffset)
        val horaInicio = estado.tramoSeleccionado.first
        val horaFin = estado.tramoSeleccionado.second

        if (verificarSolapamiento(obs.id, estado.accesoSeleccionado, fechaCalculada, horaInicio, horaFin, turnoIdEditando)) {
            _uiState.update { it.copy(mensajeError = "⚠️ Conflicto: El guardia ya está ocupado o esta puerta ya tiene un turno a esa hora.") }
            return false
        }

        if (turnoIdEditando > 0L) {
            val exito = actualizar(turnoIdEditando, obs.id, estado.accesoSeleccionado, horaInicio, horaFin, fechaCalculada)
            if (exito <= 0) {
                _uiState.update { it.copy(mensajeError = "Error al actualizar el turno en la base de datos.") }
                return false
            }
        } else {
            val idNuevo = insertar(obs.id, estado.accesoSeleccionado, horaInicio, horaFin, fechaCalculada)
            if (idNuevo == -1L) {
                _uiState.update { it.copy(mensajeError = "Error al guardar el turno en la base de datos.") }
                return false
            }
        }

        _uiState.update { it.copy(asignacionExitosa = true, mensajeError = null) }
        return true
    }
}
