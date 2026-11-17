package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.RegistroUiState
import com.example.appconteo.model.RolEnum
import com.example.appconteo.model.Usuario
import com.example.appconteo.model.Utilidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreCambia(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, errorNombre = null) }
    }

    fun onCorreoCambia(correo: String) {
        _uiState.update { it.copy(correo = correo, errorCorreo = null) }
    }

    fun onContrasenaCambia(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorContrasena = null) }
    }

    fun onPalabraSecretaCambia(palabra: String) {
        _uiState.update { it.copy(palabraSecreta = palabra, errorPalabraSecreta = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onTerminosCambia(acepta: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = acepta, errorTerminos = null) }
    }

    fun onCorreoRepetido() {
        _uiState.update { it.copy(errorCorreo = "Este correo ya se encuentra registrado") }
    }

    fun onRegistrar(): Usuario? {
        val estado = _uiState.value
        var valido = true

        var errNombre: String? = null
        var errCorreo: String? = null
        var errContrasena: String? = null
        var errPalabra: String? = null
        var errTerminos: String? = null

        if (estado.nombre.trim().isEmpty()) {
            errNombre = "El nombre es obligatorio"
            valido = false
        }

        val correoLimpio = estado.correo.trim()
        if (correoLimpio.isEmpty() || !Utilidades.esCorreoValido(correoLimpio)) {
            errCorreo = "Ingresa un correo válido"
            valido = false
        }

        if (!estado.contrasenaValida) {
            errContrasena = "La contraseña debe cumplir todos los requisitos de seguridad"
            valido = false
        }

        if (estado.palabraSecreta.trim().isEmpty()) {
            errPalabra = "La palabra secreta es obligatoria"
            valido = false
        }

        if (!estado.aceptaTerminos) {
            errTerminos = "Debes aceptar los términos y condiciones"
            valido = false
        }

        if (!valido) {
            _uiState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorCorreo = errCorreo,
                    errorContrasena = errContrasena,
                    errorPalabraSecreta = errPalabra,
                    errorTerminos = errTerminos
                )
            }
            return null
        }

        return Usuario(
            nombre = estado.nombre.trim(),
            correo = correoLimpio,
            contrasena = estado.contrasena.trim(),
            palabraSecreta = estado.palabraSecreta.trim(),
            rolId = RolEnum.OBSERVADOR.id,
            rolNombre = RolEnum.OBSERVADOR.nombre
        )
    }

    fun onLimpiar() {
        _uiState.value = RegistroUiState()
    }
}
