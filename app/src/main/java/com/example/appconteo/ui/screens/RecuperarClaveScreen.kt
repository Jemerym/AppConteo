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
import com.example.appconteo.R
import com.example.appconteo.model.Utilidades
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@Composable
fun RecuperarClaveScreen(
    onRestablecerClave: (String, String, String) -> Boolean,
    onVolverLogin: () -> Unit
) {
    RecuperarClaveContent(
        onVolverLogin = onVolverLogin,
        onRestablecerClave = onRestablecerClave
    )
}

@Composable
fun RecuperarClaveContent(
    onVolverLogin: () -> Unit,
    onRestablecerClave: (String, String, String) -> Boolean
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var palabraSecreta by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var repetirClave by remember { mutableStateOf("") }
    var nuevaVisible by remember { mutableStateOf(false) }
    var repetirVisible by remember { mutableStateOf(false) }

    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorPalabra by remember { mutableStateOf<String?>(null) }
    var errorNueva by remember { mutableStateOf<String?>(null) }
    var errorRepetir by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf(false) }

    val regexContrasena = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]).{8,}\$")

    fun validarYRestablecer() {
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

        if (nuevaClave.isBlank() || !nuevaClave.matches(regexContrasena)) {
            errorNueva = "Mínimo 8 caracteres, 1 Mayúscula, 1 Minúscula, 1 Número y 1 Carácter Especial"
            valido = false
        } else {
            errorNueva = null
        }

        if (repetirClave != nuevaClave) {
            errorRepetir = "Las contraseñas no coinciden"
            valido = false
        } else {
            errorRepetir = null
        }

        if (valido) {
            val cambiado = onRestablecerClave(correo, palabraSecreta, nuevaClave)
            if (cambiado) {
                exito = true
                Toast.makeText(context, "✅ Contraseña restablecida con éxito", Toast.LENGTH_SHORT).show()
            } else {
                exito = false
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

                CampoContrasena(
                    valor = nuevaClave,
                    onValorCambia = {
                        nuevaClave = it
                        errorNueva = null
                    },
                    etiqueta = "Nueva Contraseña",
                    visible = nuevaVisible,
                    onAlternarVisible = { nuevaVisible = !nuevaVisible },
                    error = errorNueva
                )

                CampoContrasena(
                    valor = repetirClave,
                    onValorCambia = {
                        repetirClave = it
                        errorRepetir = null
                    },
                    etiqueta = "Repetir Nueva Contraseña",
                    visible = repetirVisible,
                    onAlternarVisible = { repetirVisible = !repetirVisible },
                    error = errorRepetir
                )
            }

            Spacer(Modifier.height(Dimens.espacioGrande))

            if (!exito) {
                BotonPrincipal(
                    texto = "Actualizar Contraseña",
                    onClick = { validarYRestablecer() },
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Dimens.espacioMedio))
            }

            BotonSecundario(
                texto = "Volver al Login",
                onClick = onVolverLogin,
                modifier = Modifier.fillMaxWidth()
            )

            if (exito) {
                Spacer(Modifier.height(Dimens.espacioGrande))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                        Text(
                            text = "✅ Contraseña Actualizada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Ya puedes iniciar sesión con tu nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
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
            onRestablecerClave = { _, _, _ -> true }
        )
    }
}
