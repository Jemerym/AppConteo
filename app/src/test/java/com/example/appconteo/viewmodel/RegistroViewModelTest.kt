package com.example.appconteo.viewmodel

import com.example.appconteo.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RegistroViewModelTest {

    // Llena el formulario con datos correctos
    private fun formularioValido(viewModel: RegistroViewModel) {
        viewModel.onNombreCambia("Camila Rojas")
        viewModel.onCorreoCambia("camila@duocuc.cl")
        viewModel.onContrasenaCambia("Clave#123")
        viewModel.onRepetirCambia("Clave#123")
        viewModel.onPalabraSecretaCambia("duoc")
        viewModel.onTerminosCambia(true)
    }

    @Test
    fun registrar_conContrasenasDistintas_devuelveNullYMuestraError() {
        val viewModel = RegistroViewModel()
        formularioValido(viewModel)
        viewModel.onRepetirCambia("Otra#123")

        val usuario = viewModel.onRegistrar()

        assertNull(usuario)
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.errorRepetir)
    }

    @Test
    fun registrar_conDatosValidos_devuelveUsuarioObservador() {
        val viewModel = RegistroViewModel()
        formularioValido(viewModel)

        val usuario = viewModel.onRegistrar()

        assertNotNull(usuario)
        assertEquals("camila@duocuc.cl", usuario!!.correo)
        assertEquals(Rol.ID_OBSERVADOR, usuario.rolId)
    }
}
