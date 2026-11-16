package com.example.appconteo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.Utilidades
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.UsuariosViewModel

@Composable
fun RecuperarClaveScreen(
    onVolverLogin: () -> Unit,
    usuariosViewModel: UsuariosViewModel = viewModel()
) {
    RecuperarClaveContent(
        onVolverLogin = onVolverLogin,
        onVerificarClave = { correo, palabra -> usuariosViewModel.recuperarClave(correo, palabra) }
    )
}

@Composable
fun RecuperarClaveContent(
    onVolverLogin: () -> Unit,
    onVerificarClave: (String, String) -> String?
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var palabraSecreta by remember { mutableStateOf("") }

    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorPalabra by remember { mutableStateOf<String?>(null) }
    var claveRecuperada by remember { mutableStateOf<String?>(null) }

    fun validarYVerificar() {
        var valido = true

        if (correo.isBlank() || !Utilidades.esCorreoValido(correo)) {
            errorCorreo = "Ingresa un correo válido"
            valido = false
        } else {
            errorCorreo = null
        }

        if (palabraSecreta.isBlank()) {
            errorPalabra = "Ingresa tu palabra secreta"
            valido = false
        } else {
            errorPalabra = null
        }

        if (valido) {
            val clave = onVerificarClave(correo, palabraSecreta)
            if (clave != null) {
                claveRecuperada = clave
                Toast.makeText(context, "✅ Verificación exitosa", Toast.LENGTH_SHORT).show()
            } else {
                claveRecuperada = null
                errorCorreo = "Correo o palabra secreta incorrectos"
                errorPalabra = "Correo o palabra secreta incorrectos"
            }
        }
    }

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
                text = "Recuperar Clave",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            Text(
                text = "Ingresa tu correo y tu palabra secreta configurada al registrarte.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.espacioChico)
            )

            Spacer(Modifier.height(Dimens.espacioGrande))

            TarjetaSeccion(titulo = "Verificación de Seguridad") {
                CampoTexto(
                    valor = correo,
                    onValorCambia = {
                        correo = it
                        errorCorreo = null
                    },
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                CampoTexto(
                    valor = palabraSecreta,
                    onValorCambia = {
                        palabraSecreta = it
                        errorPalabra = null
                    },
                    etiqueta = "Palabra Secreta / Pregunta",
                    icono = R.drawable.ic_candado,
                    error = errorPalabra
                )
            }

            Spacer(Modifier.height(Dimens.espacioGrande))

            BotonPrincipal(
                texto = "Recuperar Contraseña",
                onClick = { validarYVerificar() },
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            BotonSecundario(
                texto = "Volver al Login",
                onClick = onVolverLogin,
                modifier = Modifier.fillMaxWidth()
            )

            claveRecuperada?.let { clave ->
                Spacer(Modifier.height(Dimens.espacioGrande))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                        Text(
                            text = "✅ Verificación Exitosa",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tu contraseña es: $clave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarClavePreview() {
    AppConteoTheme {
        RecuperarClaveContent(
            onVolverLogin = {},
            onVerificarClave = { _, _ -> "123456" }
        )
    }
}
