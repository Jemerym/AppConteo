package com.example.appconteo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appconteo.model.CambiosPerfil
import com.example.appconteo.model.EditarPerfilUiState
import com.example.appconteo.model.Usuario
import com.example.appconteo.model.Validaciones
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

    // CAMBIO: antes comparaba el hash de la contraseña actual aquí, en la app.
    // Ahora solo valida el formato; el servidor es quien verifica que la contraseña actual sea correcta.
    fun validarYGuardar(): CambiosPerfil? {
        val estado = _uiState.value
        val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")

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
        }

        if (estado.nuevaContrasena.isNotBlank()) {
            val errorPolitica = Validaciones.contrasena(estado.nuevaContrasena)
            if (errorPolitica != null) {
                errorNueva = errorPolitica
                valido = false
            } else if (estado.nuevaContrasena != estado.confirmarNuevaContrasena) {
                errorConfirmar = "Las contraseñas no coinciden"
                valido = false
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

        return CambiosPerfil(
            nombre = estado.nombre.trim(),
            contrasenaActual = estado.contrasenaActual,
            nuevaContrasena = estado.nuevaContrasena.ifBlank { null }
        )
    }

    // Muestra el error que devolvió el servidor junto al campo que corresponde.
    fun onErrorServidor(mensaje: String) {
        _uiState.update {
            when {
                mensaje.contains("contraseña actual", ignoreCase = true) -> it.copy(errorContrasenaActual = mensaje)
                mensaje.contains("contraseña", ignoreCase = true) -> it.copy(errorNuevaContrasena = mensaje)
                else -> it.copy(errorNombre = mensaje)
            }
        }
    }
}
