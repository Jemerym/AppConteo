package com.example.appconteo.viewmodel

import com.example.appconteo.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ProgramarViewModelTest {

    @Test
    fun programar_sinObservador_muestraErrorYNoLlamaAlServidor() {
        val viewModel = ProgramarViewModel()
        var seLlamoAlServidor = false

        viewModel.programarTurno { _, _, _, _, _, _, _, _ -> seLlamoAlServidor = true }

        assertEquals("⚠️ Selecciona un observador.", viewModel.uiState.value.mensajeError)
        assertFalse(seLlamoAlServidor)
    }

    @Test
    fun programar_cuandoElServidorRechaza_muestraSuMensaje() {
        val viewModel = ProgramarViewModel()
        viewModel.onObservadorSeleccionado(Usuario(id = 2, nombre = "Camila", correo = "camila@duocuc.cl"))

        // Simulamos el servidor: responde con error (por ejemplo, turno que choca)
        viewModel.programarTurno { _, _, _, _, _, _, _, onError -> onError("Conflicto de horario") }

        assertEquals("⚠️ Conflicto de horario", viewModel.uiState.value.mensajeError)
        assertFalse(viewModel.uiState.value.asignacionExitosa)
    }
}
