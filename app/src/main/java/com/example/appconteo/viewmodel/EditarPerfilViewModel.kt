package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.EditarPerfilUiState
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditarPerfilViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    private var inicializado = false

    fun inicializar(usuario: Usuario) {
        if (!inicializado) {
            inicializado = true
            _uiState.update { it.copy(nombre = usuario.nombre) }
        }
    }

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    }

    fun onContrasenaActualCambia(valor: String) {
        _uiState.update { it.copy(contrasenaActual = valor, errorContrasenaActual = null) }
    }

    fun onNuevaContrasenaCambia(valor: String) {
        _uiState.update { it.copy(nuevaContrasena = valor, errorNuevaContrasena = null) }
    }

    fun onConfirmarCambia(valor: String) {
        _uiState.update { it.copy(confirmarNuevaContrasena = valor, errorConfirmar = null) }
    }

    fun onAlternarActual() {
        _uiState.update { it.copy(contrasenaActualVisible = !it.contrasenaActualVisible) }
    }

    fun onAlternarNueva() {
        _uiState.update { it.copy(nuevaContrasenaVisible = !it.nuevaContrasenaVisible) }
    }

    fun onAlternarConfirmar() {
        _uiState.update { it.copy(confirmarVisible = !it.confirmarVisible) }
    }

    fun validarYGuardar(usuarioOriginal: Usuario): Usuario? {
        val estado = _uiState.value
        val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
        val regexContrasena = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]).{8,}\$")

        var errorNombre: String? = null
        var errorActual: String? = null
        var errorNueva: String? = null
        var errorConfirmar: String? = null
        var valido = true

        if (estado.nombre.isBlank()) {
            errorNombre = "El nombre es obligatorio"
            valido = false
        } else if (!estado.nombre.matches(regexNombre)) {
            errorNombre = "El nombre solo debe contener letras"
            valido = false
        }

        if (estado.contrasenaActual.isBlank()) {
            errorActual = "Ingresa tu contraseña actual"
            valido = false
        } else if (usuarioOriginal.contrasena != estado.contrasenaActual) {
            errorActual = "Contraseña actual incorrecta"
            valido = false
        }

        var nuevaContra = usuarioOriginal.contrasena
        if (estado.nuevaContrasena.isNotBlank()) {
            if (!estado.nuevaContrasena.matches(regexContrasena)) {
                errorNueva = "Mínimo 8 caracteres, 1 Mayúscula, 1 Minúscula, 1 Número y 1 Carácter Especial"
                valido = false
            } else if (estado.nuevaContrasena != estado.confirmarNuevaContrasena) {
                errorConfirmar = "Las contraseñas no coinciden"
                valido = false
            } else {
                nuevaContra = estado.nuevaContrasena
            }
        }

        if (!valido) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorContrasenaActual = errorActual,
                    errorNuevaContrasena = errorNueva,
                    errorConfirmar = errorConfirmar
                )
            }
            return null
        }

        return usuarioOriginal.copy(
            nombre = estado.nombre.trim(),
            contrasena = nuevaContra
        )
    }
}
