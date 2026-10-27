package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.appconteo.R
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.CampoContrasena
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    usuario: Usuario,
    onVolver: () -> Unit,
    onGuardar: (Usuario) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(usuario.nombre) }
    var contrasenaActual by rememberSaveable { mutableStateOf("") }
    var nuevaContrasena by rememberSaveable { mutableStateOf("") }
    var confirmarNuevaContrasena by rememberSaveable { mutableStateOf("") }
    var actualVisible by rememberSaveable { mutableStateOf(false) }
    var nuevaVisible by rememberSaveable { mutableStateOf(false) }
    var confirmarVisible by rememberSaveable { mutableStateOf(false) }
    var errorContrasenaActual by remember { mutableStateOf<String?>(null) }
    var errorNuevaContrasena by remember { mutableStateOf<String?>(null) }
    var errorConfirmar by remember { mutableStateOf<String?>(null) }
    var errorNombre by remember { mutableStateOf<String?>(null) }

    val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
    val regexContrasena = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]).{8,}\$")
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
                            if (nombre.isBlank()) {
                                errorNombre = "El nombre es obligatorio"
                                return@IconButton
                            }
                            if (!nombre.matches(regexNombre)) {
                                errorNombre = "El nombre solo debe contener letras"
                                return@IconButton
                            }
                            if (contrasenaActual.isBlank()) {
                                errorContrasenaActual = "Ingresa tu contraseña actual"
                                return@IconButton
                            }
                            if (usuario.contrasena != contrasenaActual) {
                                errorContrasenaActual = "Contraseña actual incorrecta"
                                return@IconButton
                            }
                            var NuevaContra = usuario.contrasena
                            if (nuevaContrasena.isNotBlank()) {
                                if (!nuevaContrasena.matches(regexContrasena)) {
                                    errorNuevaContrasena = "Mínimo 8 caracteres, 1 Mayúscula, 1 Minúscula, 1 Número y 1 Carácter Especial"
                                    return@IconButton
                                }
                                if (nuevaContrasena != confirmarNuevaContrasena) {
                                    errorConfirmar = "Las contraseñas no coinciden"
                                    return@IconButton
                                }
                                NuevaContra = nuevaContrasena
                            }

                            onGuardar(usuario.copy(nombre = nombre.trim(), contrasena = NuevaContra))
                        },
                        enabled = nombre.isNotBlank() && contrasenaActual.isNotBlank()
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
                valor = nombre,
                onValorCambia = {
                    nombre = it
                    errorNombre = null
                },
                etiqueta = "Nombre",
                icono = R.drawable.ic_persona,
                error = errorNombre
            )

            Spacer(Modifier.height(Dimens.espacioChico))
            Text("Seguridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            CampoContrasena(
                valor = contrasenaActual,
                onValorCambia = {
                    contrasenaActual = it
                    errorContrasenaActual = null
                },
                etiqueta = "Contraseña Actual *",
                visible = actualVisible,
                onAlternarVisible = { actualVisible = !actualVisible },
                error = errorContrasenaActual
            )

            CampoContrasena(
                valor = nuevaContrasena,
                onValorCambia = {
                    nuevaContrasena = it
                    errorNuevaContrasena = null
                },
                etiqueta = "Nueva Contraseña (Opcional)",
                visible = nuevaVisible,
                onAlternarVisible = { nuevaVisible = !nuevaVisible },
                error = errorNuevaContrasena
            )

            CampoContrasena(
                valor = confirmarNuevaContrasena,
                onValorCambia = {
                    confirmarNuevaContrasena = it
                    errorConfirmar = null
                },
                etiqueta = "Confirmar Nueva Contraseña",
                visible = confirmarVisible,
                onAlternarVisible = { confirmarVisible = !confirmarVisible },
                error = errorConfirmar
            )
        }
    }
}