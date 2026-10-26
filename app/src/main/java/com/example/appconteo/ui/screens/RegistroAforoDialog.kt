package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.style.EstilosBoton
import com.example.appconteo.ui.style.estiloAltoBoton
import com.example.appconteo.ui.theme.Blanco
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RegistroAforoDialog(
    onDismiss: () -> Unit,
    onGuardar: ( tipo: String, rangoEdad: String) -> Unit,
    onGuardarYCerrar: ( tipo: String, rangoEdad: String) -> Unit
) {
    var cantidad by remember { mutableStateOf("1") }
    var tipoGrupo by remember { mutableStateOf("Solo") }
    var rangoEdad by remember { mutableStateOf("25-34") }
    var mensajeExito by remember { mutableStateOf(false) }

    val tipos = listOf("Solo", "Acompañado", "Grupo (3+)")
    val edades = listOf("< 18", "18-24", "25-34", "35-50", "+ 50")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro Aforo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Tipo de grupo:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    tipos.forEach { tipo ->
                        FilterChip(
                            selected = tipoGrupo == tipo,
                            onClick = { tipoGrupo = tipo },
                            label = { Text(tipo, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text("Rango de edad:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                    edades.chunked(3).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            fila.forEach { edad ->
                                FilterChip(
                                    selected = rangoEdad == edad,
                                    onClick = { rangoEdad = edad },
                                    label = { Text(edad) }
                                )
                            }
                        }
                    }
                }

                if (mensajeExito) {
                    Text(
                        text = "¡Registrado! Listo para el siguiente...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                Button(
                    onClick = {
                        val cant = cantidad.toIntOrNull() ?: 1
                        onGuardar(tipoGrupo, rangoEdad)
                        cantidad = "1"
                        mensajeExito = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .estiloAltoBoton(),
                    shape = EstilosBoton.forma,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Blanco
                    )
                ) {
                    Text("Guardar y Seguir")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    BotonSecundario(
                        texto = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    BotonSecundario(
                        texto = "Guardar ",
                        onClick = {
                            val cant = cantidad.toIntOrNull() ?: 1
                            onGuardarYCerrar( tipoGrupo, rangoEdad)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, name = "Preview - Diálogo Aforo Continuo")
@Composable
private fun AforoDialogPreview() {
    AppConteoTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            RegistroAforoDialog(
                onDismiss = {},
                onGuardar = { _, _ -> },
                onGuardarYCerrar = {_,_ -> }
            )
        }
    }
}