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

    // Borra solo los mensajes de error (se usa al salir hacia Registro o Recuperar clave)
    fun limpiarErrores() {
        _uiState.update { it.copy(errorCorreo = null, errorContrasena = null, loginExitoso = false) }
    }

    // Deja la pantalla como nueva: campos vacíos y sin errores
    fun limpiar() {
        _uiState.value = LoginUiState()
    }

    // CAMBIO: antes iniciarSesion devolvía Boolean al instante (SQLite).
    // Ahora la respuesta del servidor tarda, así que se avisa por callbacks:
    //   onExito -> marca loginExitoso (la pantalla navega al observarlo)
    //   onError -> muestra el mensaje que mandó el servidor
    fun onLogin(iniciarSesion: (String, String, () -> Unit, (String) -> Unit) -> Unit) {
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
            return
        }

        iniciarSesion(
            estado.correo,
            estado.contrasena,
            { _uiState.update { it.copy(loginExitoso = true) } },
            { mensaje -> _uiState.update { it.copy(errorCorreo = mensaje, errorContrasena = mensaje) } }
        )
    }
}
