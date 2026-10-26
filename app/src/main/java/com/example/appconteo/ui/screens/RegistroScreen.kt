package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.RegistroUiState
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.CasillaConTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.ui.theme.Exito
import com.example.appconteo.ui.theme.ExitoFondo
import com.example.appconteo.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    onRegistroExitoso: (Usuario) -> Unit,
    onIrLogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    RegistroContent(
        uiState = uiState,
        onNombreCambia = viewModel::onNombreCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onConfirmarContrasenaCambia = viewModel::onConfirmarContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onAlternarVisibilidadConfirmacion = viewModel::onAlternarVisibilidadConfirmacion,
        onTerminosCambia = viewModel::onTerminosCambia,
        onRegistrar = { viewModel.onRegistrar()?.let { usuario -> onRegistroExitoso(usuario) } },
        onLimpiar = viewModel::onLimpiar,
        onIrLogin = onIrLogin,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegistroContent(
    uiState: RegistroUiState,
    onNombreCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onConfirmarContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onAlternarVisibilidadConfirmacion: () -> Unit,
    onTerminosCambia: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onIrLogin: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(Dimens.espacioGrande))

            // Título centrado idéntico al Login
            Text(
                text = stringResource(R.string.titulo_formulario),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(Dimens.espacioGrande))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                    CampoTexto(
                        valor = uiState.nombre,
                        onValorCambia = onNombreCambia,
                        etiqueta = stringResource(R.string.campo_nombre),
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombre
                    )
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoContrasena(
                        valor = uiState.contrasena,
                        onValorCambia = onContrasenaCambia,
                        etiqueta = stringResource(R.string.campo_contrasena),
                        visible = uiState.contrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidad,
                        error = uiState.errorContrasena
                    )
                    CampoContrasena(
                        valor = uiState.confirmarContrasena,
                        onValorCambia = onConfirmarContrasenaCambia,
                        etiqueta = stringResource(R.string.campo_confirmar_contrasena),
                        visible = uiState.confirmarContrasenaVisible,
                        onAlternarVisible = onAlternarVisibilidadConfirmacion,
                        error = uiState.errorConfirmarContrasena
                    )
                }

                Column {
                    CasillaConTexto(
                        texto = stringResource(R.string.opcion_terminos),
                        marcado = uiState.aceptaTerminos,
                        onCambio = onTerminosCambia
                    )

                    uiState.errorTerminos?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_registrar),
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ya_tienes_cuenta),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onIrLogin) {
                        Text(stringResource(R.string.btn_iniciar_sesion))
                    }
                }

                if (uiState.registroExitoso) {
                    MensajeExito(nombre = uiState.nombre)
                }

                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

@Composable
private fun MensajeExito(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = stringResource(R.string.registro_exitoso, nombre),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistroVacioPreview() {
    AppConteoTheme {
        RegistroContent(
            uiState = RegistroUiState(),
            onNombreCambia = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onAlternarVisibilidadConfirmacion = {},
            onTerminosCambia = {}, onRegistrar = {}, onLimpiar = {},
            onIrLogin = {}
        )
    }
}

@Preview(showBackground = true, name = "Con errores")
@Composable
private fun RegistroConErroresPreview() {
    AppConteoTheme {
        RegistroContent(
            uiState = RegistroUiState(
                correo = "ana@",
                errorNombre = "El nombre es obligatorio",
                errorCorreo = "Ingresa un correo válido",
                errorContrasena = "Mínimo 6 caracteres",
                errorConfirmarContrasena = "Las contraseñas no coinciden",
                errorTerminos = "Debes aceptar los términos"
            ),
            onNombreCambia = {}, onCorreoCambia = {}, onContrasenaCambia = {},
            onConfirmarContrasenaCambia = {},
            onAlternarVisibilidad = {}, onAlternarVisibilidadConfirmacion = {},
            onTerminosCambia = {}, onRegistrar = {}, onLimpiar = {},
            onIrLogin = {}
        )
    }
}