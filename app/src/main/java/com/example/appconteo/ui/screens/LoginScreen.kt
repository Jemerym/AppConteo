package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.appconteo.R
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.theme.Dimens

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrRegistro: () -> Unit,
    onLoginIntentar: (String) -> Boolean
) {
    var correo by remember { mutableStateOf("") }
    var errorCorreo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            Spacer(Modifier.height(Dimens.espacioMedio))

            BotonPrincipal(
                texto = stringResource(R.string.btn_iniciar_sesion),
                onClick = {
                    if (correo.isBlank()) {
                        errorCorreo = "El correo es obligatorio"
                    } else {
                        val exito = onLoginIntentar(correo)
                        if (exito) {
                            onLoginExitoso()
                        } else {
                            errorCorreo = "Correo no registrado"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioMedio))

            TextButton(onClick = onIrRegistro) {
                Text(stringResource(R.string.no_tienes_cuenta))
            }
        }
    }
}