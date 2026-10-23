package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.FormularioUiState
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistroViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, registroExitoso = false) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, registroExitoso = false) }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, registroExitoso = false) }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmarContrasena = null, registroExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onAlternarVisibilidadConfirmacion() {
        _uiState.update { it.copy(confirmarContrasenaVisible = !it.confirmarContrasenaVisible) }
    }

    fun onTerminosCambia(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    fun onRegistrar(): Usuario? {
        val estado = _uiState.value

        val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
        val errorNombre = when {
            estado.nombre.isBlank() -> "El nombre es obligatorio"
            !estado.nombre.matches(regexNombre) -> "El nombre solo debe contener letras"
            else -> null
        }

        val errorCorreo = if (!estado.correo.contains("@") || !estado.correo.contains("."))
            "Ingresa un correo válido" else null
        val errorContrasena = if (estado.contrasena.length < 6)
            "Mínimo 6 caracteres" else null

        val errorConfirmarContrasena = when {
            estado.confirmarContrasena.isBlank() -> "Confirma tu contraseña"
            estado.confirmarContrasena != estado.contrasena -> "Las contraseñas no coinciden"
            else -> null
        }

        val errorTerminos = if (!estado.aceptaTerminos)
            "Debes aceptar los términos" else null

        val hayErrores = listOf(errorNombre, errorCorreo, errorContrasena, errorConfirmarContrasena, errorTerminos)
            .any { it != null }

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorContrasena = errorContrasena,
                errorConfirmarContrasena = errorConfirmarContrasena,
                errorTerminos = errorTerminos,
                registroExitoso = !hayErrores
            )
        }

        return if (hayErrores) null else Usuario(estado.nombre, estado.correo, contrasena = estado.contrasena)
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}