package com.example.appconteo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun loginConCamposVacios_muestraErroresYNoLlamaAlServidor() {
        val viewModel = LoginViewModel()
        var seLlamoAlServidor = false

        viewModel.onLogin { _, _, _, _ -> seLlamoAlServidor = true }

        val estado = viewModel.uiState.value
        assertEquals("El correo es obligatorio", estado.errorCorreo)
        assertEquals("La contraseña es obligatoria", estado.errorContrasena)
        assertFalse(seLlamoAlServidor)
    }

    @Test
    fun loginConDatosValidos_cuandoElServidorAcepta_marcaLoginExitoso() {
        val viewModel = LoginViewModel()
        viewModel.onCorreoLogin("admin@demo.cl")
        viewModel.onContrasenaLogin("1234")

        // Simulamos el servidor: llama a onExito
        viewModel.onLogin { _, _, onExito, _ -> onExito() }

        assertTrue(viewModel.uiState.value.loginExitoso)
    }
}
