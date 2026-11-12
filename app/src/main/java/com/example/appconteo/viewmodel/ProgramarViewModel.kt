package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.ProgramarUiState
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgramarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = UsuarioDbHelper(application)

    private val _uiState = MutableStateFlow(ProgramarUiState())
    val uiState: StateFlow<ProgramarUiState> = _uiState.asStateFlow()

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

    fun programarTurno(): Boolean {
        val estado = _uiState.value
        val obs = estado.observadorSeleccionado ?: run {
            _uiState.update { it.copy(mensajeError = "⚠️ Selecciona un observador.") }
            return false
        }

        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, estado.diasOffset) }
        val fechaCalculada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val horaInicio = estado.tramoSeleccionado.first
        val horaFin = estado.tramoSeleccionado.second

        if (db.haySolapamientoTurno(obs.id, estado.accesoSeleccionado, fechaCalculada, horaInicio, horaFin)) {
            _uiState.update { it.copy(mensajeError = "⚠️ Conflicto: El guardia ya está ocupado o esta puerta ya tiene un turno a esa hora.") }
            return false
        }

        db.insertarTurno(obs.id, estado.accesoSeleccionado, horaInicio, horaFin, fechaCalculada)
        _uiState.update { it.copy(asignacionExitosa = true, mensajeError = null) }
        return true
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
