package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.EditarPerfilViewModel
import com.example.appconteo.model.CambiosPerfil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onGuardar: (CambiosPerfil, () -> Unit, (String) -> Unit) -> Unit,
    viewModel: EditarPerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario) {
        viewModel.inicializar(usuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modificar Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Valida el formato aquí; el servidor verifica la contraseña actual.
                            viewModel.validarYGuardar()?.let { cambios ->
                                onGuardar(cambios, onVolver, viewModel::onErrorServidor)
                            }
                        },
                        enabled = uiState.nombre.isNotBlank() && uiState.contrasenaActual.isNotBlank()
                    ) {
                        Icon(painterResource(R.drawable.ic_check), "Guardar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            CampoTexto(
                valor = uiState.nombre,
                onValorCambia = viewModel::onNombreCambia,
                etiqueta = "Nombre",
                icono = R.drawable.ic_persona,
                error = uiState.errorNombre
            )

            Spacer(Modifier.height(Dimens.espacioChico))
            Text("Seguridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            CampoContrasena(
                valor = uiState.contrasenaActual,
                onValorCambia = viewModel::onContrasenaActualCambia,
                etiqueta = "Contraseña Actual *",
                visible = uiState.contrasenaActualVisible,
                onAlternarVisible = viewModel::onAlternarActual,
                error = uiState.errorContrasenaActual
            )

            CampoContrasena(
                valor = uiState.nuevaContrasena,
                onValorCambia = viewModel::onNuevaContrasenaCambia,
                etiqueta = "Nueva Contraseña (Opcional)",
                visible = uiState.nuevaContrasenaVisible,
                onAlternarVisible = viewModel::onAlternarNueva,
                error = uiState.errorNuevaContrasena
            )

            CampoContrasena(
                valor = uiState.confirmarNuevaContrasena,
                onValorCambia = viewModel::onConfirmarCambia,
                etiqueta = "Confirmar Nueva Contraseña",
                visible = uiState.confirmarVisible,
                onAlternarVisible = viewModel::onAlternarConfirmar,
                error = uiState.errorConfirmar
            )
        }
    }
}
