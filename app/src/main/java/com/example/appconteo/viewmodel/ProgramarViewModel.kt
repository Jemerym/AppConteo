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

    // Al editar se llama dos veces: primero con turno = null (fija el id al instante, para que un toque
    // rápido en "Programar" no cree un turno nuevo) y luego con el turno traído del servidor.
    fun cargarTurno(turnoId: Long, turno: Turno?, buscarUsuario: (Long) -> Usuario?) {
        turnoIdEditando = turnoId
        if (turnoId > 0L) {
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

    private var guardando = false

    // CAMBIO: antes recibía 3 funciones sincrónicas (solapamiento, actualizar, insertar) y devolvía Boolean.
    // Ahora recibe UNA función que responde por callbacks: el servidor valida los conflictos (409)
    // y su mensaje se muestra tal cual.
    fun programarTurno(
        guardar: (Long, Long, Int, String, String, String, () -> Unit, (String) -> Unit) -> Unit
    ) {
        val estado = _uiState.value
        val obs = estado.observadorSeleccionado ?: run {
            _uiState.update { it.copy(mensajeError = "⚠️ Selecciona un observador.") }
            return
        }
        if (guardando) return   // ignora un segundo toque mientras responde el servidor
        guardando = true

        guardar(
            turnoIdEditando,
            obs.id,
            estado.accesoSeleccionado,
            estado.tramoSeleccionado.first,
            estado.tramoSeleccionado.second,
            Fechas.conOffset(estado.diasOffset),
            {
                guardando = false
                _uiState.update { it.copy(asignacionExitosa = true, mensajeError = null) }
            },
            { mensaje ->
                guardando = false
                _uiState.update { it.copy(mensajeError = "⚠️ $mensaje") }
            }
        )
    }
}
