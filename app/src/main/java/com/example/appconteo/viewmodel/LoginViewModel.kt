package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.LoginUiState
import com.example.appconteo.model.Utilidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoLogin(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorContrasena = null, loginExitoso = false) }
    }

    fun onContrasenaLogin(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorCorreo = null, errorContrasena = null, loginExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onLogin(iniciarSesion: (String, String) -> Boolean): Boolean {
        val estado = _uiState.value
        val errorCorreo = when {
            estado.correo.isBlank() -> "El correo es obligatorio"
            !Utilidades.esCorreoValido(estado.correo) -> "Ingresa un correo válido (ej: usuario@duocuc.cl)"
            else -> null
        }
        val errorContrasena = if (estado.contrasena.isBlank()) "La contraseña es obligatoria" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena)
            }
            return false
        }
        val exito = iniciarSesion(estado.correo, estado.contrasena)
        if (exito) {
            _uiState.update { it.copy(loginExitoso = true) }
            return true
        } else {
            val mensajeError = "Correo o contraseña incorrectos"
            _uiState.update {
                it.copy(
                    errorCorreo = mensajeError,
                    errorContrasena = mensajeError
                )
            }
            return false
        }
    }
}
