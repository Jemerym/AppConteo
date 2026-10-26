package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appconteo.data.SesionPreferencias
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val db = UsuarioDbHelper(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoLogin(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, loginExitoso = false) }
    }

    fun onContrasenaLogin(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, loginExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onLogin(usuariosViewModel: UsuariosViewModel): Boolean {
        val estado = _uiState.value

        val errorCorreo = if (estado.correo.isBlank()) "El correo es obligatorio" else null
        val errorContrasena = if (estado.contrasena.isBlank()) "La contraseña es obligatoria" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena)
            }
            return false
        }

        val exito = usuariosViewModel.iniciarSesion(estado.correo, estado.contrasena)
        if (exito) {
            _uiState.update { it.copy(loginExitoso = true) }
            return true
        } else {
            _uiState.update {
                it.copy(errorCorreo = "Correo o contraseña incorrectos")
            }
            return false
        }
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}