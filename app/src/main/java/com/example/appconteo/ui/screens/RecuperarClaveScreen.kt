package com.example.appconteo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import com.example.appconteo.ui.components.IndicadoresContrasena
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.RecuperarClaveUiState
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.RecuperarClaveViewModel

@Composable
fun RecuperarClaveScreen(
    onRestablecerClave: (String, String, String, () -> Unit, (String) -> Unit) -> Unit,
    onVolverLogin: () -> Unit,
    onRecuperacionExitosa: () -> Unit,
    viewModel: RecuperarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el servidor confirma el cambio, se vuelve solo al login
    LaunchedEffect(uiState.exito) {
        if (uiState.exito) onRecuperacionExitosa()
    }

    RecuperarClaveContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onPalabraCambia = viewModel::onPalabraCambia,
        onNuevaCambia = viewModel::onNuevaCambia,
        onRepetirCambia = viewModel::onRepetirCambia,
        onAlternarNueva = viewModel::onAlternarNueva,
        onAlternarRepetir = viewModel::onAlternarRepetir,
        onRestablecer = { viewModel.onRestablecer(onRestablecerClave) },
        onVolverLogin = onVolverLogin
    )
}

@Composable
fun RecuperarClaveContent(
    uiState: RecuperarClaveUiState,
    onCorreoCambia: (String) -> Unit,
    onPalabraCambia: (String) -> Unit,
    onNuevaCambia: (String) -> Unit,
    onRepetirCambia: (String) -> Unit,
    onAlternarNueva: () -> Unit,
    onAlternarRepetir: () -> Unit,
    onRestablecer: () -> Unit,
    onVolverLogin: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Restablecer Clave",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            Text(
                text = "Ingresa tu correo, palabra secreta y define tu nueva contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.espacioChico)
            )

            Spacer(Modifier.height(Dimens.espacioGrande))

            TarjetaSeccion(titulo = "Seguridad y Nueva Contraseña") {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                CampoTexto(
                    valor = uiState.palabraSecreta,
                    onValorCambia = onPalabraCambia,
                    etiqueta = "Palabra Secreta",
                    icono = R.drawable.ic_candado,
                    error = uiState.errorPalabra
                )

                CampoContrasena(
                    valor = uiState.nuevaClave,
                    onValorCambia = onNuevaCambia,
                    etiqueta = "Nueva Contraseña",
                    visible = uiState.nuevaVisible,
                    onAlternarVisible = onAlternarNueva,
                    error = uiState.errorNueva
                )

                AnimatedVisibility(visible = uiState.nuevaClave.isNotEmpty()) {
                    IndicadoresContrasena(contrasena = uiState.nuevaClave)
                }

                CampoContrasena(
                    valor = uiState.repetirClave,
                    onValorCambia = onRepetirCambia,
                    etiqueta = "Repetir Nueva Contraseña",
                    visible = uiState.repetirVisible,
                    onAlternarVisible = onAlternarRepetir,
                    error = uiState.errorRepetir
                )
            }

            Spacer(Modifier.height(Dimens.espacioGrande))

            if (!uiState.exito) {
                BotonPrincipal(
                    texto = "Actualizar Contraseña",
                    onClick = onRestablecer,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
            }

            BotonSecundario(
                texto = "Volver al Inicio de Sesión",
                onClick = onVolverLogin,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarClavePreview() {
    AppConteoTheme {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(),
            onCorreoCambia = {},
            onPalabraCambia = {},
            onNuevaCambia = {},
            onRepetirCambia = {},
            onAlternarNueva = {},
            onAlternarRepetir = {},
            onRestablecer = {},
            onVolverLogin = {}
        )
    }
}
