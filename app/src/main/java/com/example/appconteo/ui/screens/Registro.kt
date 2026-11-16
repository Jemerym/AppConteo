package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.model.RangoEtario
import com.example.appconteo.model.TipoCompania
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.style.EstilosBoton
import com.example.appconteo.ui.style.estiloAltoBoton
import com.example.appconteo.ui.theme.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAforoDialog(
    initialTipoGrupo: String = TipoCompania.SOLO.etiqueta,
    initialRangoEdad: String = RangoEtario.EDAD_25_34.etiqueta,
    mostrarBotonSeguir: Boolean = true,
    onDismiss: () -> Unit,
    onGuardar: (tipo: String, rangoEdad: String) -> Unit = { _, _ -> },
    onGuardarYCerrar: (tipo: String, rangoEdad: String) -> Unit
) {
    var tipoGrupo by remember { mutableStateOf(initialTipoGrupo) }
    var rangoEdad by remember { mutableStateOf(initialRangoEdad) }
    var mensajeExito by remember { mutableStateOf(false) }

    val tipos = TipoCompania.entries
    val edades = RangoEtario.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mostrarBotonSeguir) "Registro Aforo" else "Modificar Registro") },
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
                            selected = tipoGrupo == tipo.etiqueta,
                            onClick = { tipoGrupo = tipo.etiqueta },
                            label = { Text(tipo.etiqueta, style = MaterialTheme.typography.labelSmall) }
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
                                    selected = rangoEdad == edad.etiqueta,
                                    onClick = { rangoEdad = edad.etiqueta },
                                    label = { Text(edad.etiqueta) }
                                )
                            }
                        }
                    }
                }

                if (mensajeExito && mostrarBotonSeguir) {
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
                if (mostrarBotonSeguir) {
                    Button(
                        onClick = {
                            onGuardar(tipoGrupo, rangoEdad)
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

                    BotonPrincipal(
                        texto = "Guardar",
                        onClick = {
                            onGuardarYCerrar(tipoGrupo, rangoEdad)
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
                onGuardarYCerrar = { _, _ -> }
            )
        }
    }
}
