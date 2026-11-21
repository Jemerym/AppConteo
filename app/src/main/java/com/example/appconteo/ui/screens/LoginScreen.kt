package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.LoginUiState
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onIniciarSesion: (String, String, () -> Unit, (String) -> Unit) -> Unit,
    onLoginExitoso: () -> Unit,
    onIrRegistro: () -> Unit,
    onIrRecuperarClave: () -> Unit = {},
    limpiarCampos: Boolean = false,
    onCamposLimpiados: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // La respuesta del servidor llega después: se navega cuando el estado marca loginExitoso.
    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) onLoginExitoso()
    }

    // Tras recuperar la clave, el login vuelve con los campos vacíos
    LaunchedEffect(limpiarCampos) {
        if (limpiarCampos) {
            viewModel.limpiar()
            onCamposLimpiados()
        }
    }

    LoginContent(
        uiState = uiState,
        onCorreoLogin = viewModel::onCorreoLogin,
        onContrasenaLogin = viewModel::onContrasenaLogin,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onLogin = { viewModel.onLogin(onIniciarSesion) },
        onIrRegistro = {
            viewModel.limpiarErrores()   // al volver, no deben quedar los errores del intento anterior
            onIrRegistro()
        },
        onIrRecuperarClave = {
            viewModel.limpiarErrores()
            onIrRecuperarClave()
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoLogin: (String) -> Unit,
    onContrasenaLogin: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onLogin: () -> Unit,
    onIrRegistro: () -> Unit,
    onIrRecuperarClave: () -> Unit = {},
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.titulo_login),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(Dimens.espacioGrande))

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoLogin,
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                error = uiState.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaLogin,
                etiqueta = stringResource(R.string.campo_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorContrasena,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = onIrRecuperarClave,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(Dimens.espacioMedio))

            BotonPrincipal(
                texto = stringResource(R.string.btn_iniciar_sesion),
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onIrRegistro) {
                    Text(stringResource(R.string.btn_registrarse))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginVacioPreview() {
    AppConteoTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoLogin = {},
            onContrasenaLogin = {},
            onAlternarVisibilidad = {},
            onLogin = {},
            onIrRegistro = {}
        )
    }
}

@Preview(showBackground = true, name = "Login con Errores")
@Composable
private fun LoginConErroresPreview() {
    AppConteoTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "correo@ejemplo.com",
                errorCorreo = "aaasdass"
            ),
            onCorreoLogin = {},
            onContrasenaLogin = {},
            onAlternarVisibilidad = {},
            onLogin = {},
            onIrRegistro = {}
        )
    }
}