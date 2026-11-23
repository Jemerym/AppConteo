package com.example.appconteo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EditarPerfilViewModelTest {

    @Test
    fun guardar_conNombreConNumeros_devuelveNullYMuestraError() {
        val viewModel = EditarPerfilViewModel()
        viewModel.onNombreCambia("Camila123")
        viewModel.onContrasenaActualCambia("Clave#123")

        val cambios = viewModel.validarYGuardar()

        assertNull(cambios)
        assertEquals("El nombre solo debe contener letras", viewModel.uiState.value.errorNombre)
    }

    @Test
    fun guardar_sinNuevaContrasena_devuelveCambiosSoloConElNombre() {
        val viewModel = EditarPerfilViewModel()
        viewModel.onNombreCambia("Camila Rojas")
        viewModel.onContrasenaActualCambia("Clave#123")

        val cambios = viewModel.validarYGuardar()

        assertNotNull(cambios)
        assertEquals("Camila Rojas", cambios!!.nombre)
        assertNull(cambios.nuevaContrasena)
    }
}
