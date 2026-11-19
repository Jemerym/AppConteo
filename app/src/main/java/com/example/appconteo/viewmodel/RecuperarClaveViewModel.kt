package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.RecuperarClaveUiState
import com.example.appconteo.model.Utilidades
import com.example.appconteo.model.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarClaveViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(v: String) = _uiState.update { it.copy(correo = v.trim(), errorCorreo = null) }
    fun onPalabraCambia(v: String) = _uiState.update { it.copy(palabraSecreta = v, errorPalabra = null) }
    fun onNuevaCambia(v: String) = _uiState.update { it.copy(nuevaClave = v, errorNueva = null) }
    fun onRepetirCambia(v: String) = _uiState.update { it.copy(repetirClave = v, errorRepetir = null) }

    fun onAlternarNueva() = _uiState.update { it.copy(nuevaVisible = !it.nuevaVisible) }
    fun onAlternarRepetir() = _uiState.update { it.copy(repetirVisible = !it.repetirVisible) }


    // CAMBIO: restablecer ya no devuelve Boolean; el servidor responde por callbacks.
    fun onRestablecer(restablecer: (String, String, String, () -> Unit, (String) -> Unit) -> Unit) {
        val e = _uiState.value
        val errCorreo = if (!Utilidades.esCorreoValido(e.correo)) "Ingresa un correo válido" else null
        val errPalabra = if (e.palabraSecreta.isBlank()) "Ingresa tu palabra secreta" else null
        val errNueva = Validaciones.contrasena(e.nuevaClave)
        val errRepetir = if (e.repetirClave != e.nuevaClave) "Las contraseñas no coinciden" else null

        _uiState.update {
            it.copy(
                errorCorreo = errCorreo,
                errorPalabra = errPalabra,
                errorNueva = errNueva,
                errorRepetir = errRepetir
            )
        }
        if (listOf(errCorreo, errPalabra, errNueva, errRepetir).any { it != null }) return

        restablecer(
            e.correo,
            e.palabraSecreta,
            e.nuevaClave,
            { _uiState.update { it.copy(exito = true) } },
            { mensaje ->
                _uiState.update {
                    if (mensaje.contains("contraseña", ignoreCase = true)) {
                        it.copy(errorNueva = mensaje)
                    } else {
                        it.copy(errorCorreo = mensaje, errorPalabra = mensaje)
                    }
                }
            }
        )
    }
}
