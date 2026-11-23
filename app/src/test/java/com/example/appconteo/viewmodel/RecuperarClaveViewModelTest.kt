package com.example.appconteo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecuperarClaveViewModelTest {

    @Test
    fun restablecer_conCorreoInvalido_muestraErrorYNoLlamaAlServidor() {
        val viewModel = RecuperarClaveViewModel()
        viewModel.onCorreoCambia("correo-sin-arroba")
        viewModel.onPalabraCambia("duoc")
        viewModel.onNuevaCambia("Clave#123")
        viewModel.onRepetirCambia("Clave#123")
        var seLlamoAlServidor = false

        viewModel.onRestablecer { _, _, _, _, _ -> seLlamoAlServidor = true }

        assertEquals("Ingresa un correo válido", viewModel.uiState.value.errorCorreo)
        assertFalse(seLlamoAlServidor)
    }

    @Test
    fun restablecer_conDatosValidos_cuandoElServidorAcepta_marcaExito() {
        val viewModel = RecuperarClaveViewModel()
        viewModel.onCorreoCambia("camila@duocuc.cl")
        viewModel.onPalabraCambia("duoc")
        viewModel.onNuevaCambia("Clave#123")
        viewModel.onRepetirCambia("Clave#123")

        // Simulamos el servidor: llama a onExito
        viewModel.onRestablecer { _, _, _, onExito, _ -> onExito() }

        assertTrue(viewModel.uiState.value.exito)
    }
}
