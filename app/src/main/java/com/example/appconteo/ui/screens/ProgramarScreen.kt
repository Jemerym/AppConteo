package com.example.appconteo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.data.ConvertidorImagen
import com.example.appconteo.model.*
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.style.EstilosCampo
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.ProgramarViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgramarScreen(
    usuariosViewModel: UsuariosViewModel = viewModel(),
    programarViewModel: ProgramarViewModel = viewModel(),
    onVolver: () -> Unit,
    onSesionProgramada: () -> Unit
) {
    val context = LocalContext.current
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val observadores = remember(usuarios) { usuarios.filter { it.rolId != 1L } }
    val uiState by programarViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(observadores) {
        if (uiState.observadorSeleccionado == null) {
            programarViewModel.onObservadorSeleccionado(observadores.firstOrNull())
        }
    }

    LaunchedEffect(uiState.asignacionExitosa) {
        if (uiState.asignacionExitosa) {
            Toast.makeText(context, "✅ Turno asignado con éxito", Toast.LENGTH_SHORT).show()
            onSesionProgramada()
        }
    }

    ProgramarContent(
        observadores = observadores,
        uiState = uiState,
        onAccesoCambia = programarViewModel::onAccesoSeleccionado,
        onObservadorCambia = programarViewModel::onObservadorSeleccionado,
        onDiasOffsetCambia = programarViewModel::onDiasOffsetCambia,
        onTramoCambia = programarViewModel::onTramoSeleccionado,
        onProgramar = {
            programarViewModel.programarTurno()
        },
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramarContent(
    observadores: List<Usuario>,
    uiState: ProgramarUiState,
    onAccesoCambia: (Int) -> Unit,
    onObservadorCambia: (Usuario?) -> Unit,
    onDiasOffsetCambia: (Int) -> Unit,
    onTramoCambia: (Pair<String, String>) -> Unit,
    onProgramar: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val tramosHorarios = remember { CatalogosRincon.tramosHorarios }
    val accesosInfo = remember { CatalogosRincon.accesosInfo }

    val fechaCalculada = remember(uiState.diasOffset) {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, uiState.diasOffset) }
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        topBar = { ProgramarTopBar(onVolver) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SelectorAccesoSection(accesosInfo, uiState.accesoSeleccionado, onAccesoCambia)
            SelectorObservadorSection(observadores, uiState.observadorSeleccionado, onObservadorCambia)
            BloqueHorarioSection(tramosHorarios, uiState.diasOffset, uiState.tramoSeleccionado, onDiasOffsetCambia, onTramoCambia)
            ResumenTurnoCard(uiState.observadorSeleccionado, accesosInfo, uiState.accesoSeleccionado, fechaCalculada, uiState.tramoSeleccionado)

            BotonPrincipal(
                texto = "Confirmar y Asignar Turno",
                onClick = onProgramar,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramarTopBar(onVolver: () -> Unit) {
    TopAppBar(
        title = { Text("Asignar Turno de Guardia", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onVolver) {
                Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun SelectorAccesoSection(accesosInfo: List<Pair<Int, String>>, accesoSeleccionado: Int, onAccesoCambia: (Int) -> Unit) {
    TarjetaSeccion(titulo = "1. Seleccionar Punto de Acceso") {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            accesosInfo.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    fila.forEach { (idAcceso, nombreAcceso) ->
                        val seleccionado = accesoSeleccionado == idAcceso
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAccesoCambia(idAcceso) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (seleccionado) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(
                                modifier = Modifier.padding(Dimens.espacioMedio),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nombreAcceso,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                    color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorObservadorSection(observadores: List<Usuario>, observadorSeleccionado: Usuario?, onObservadorCambia: (Usuario?) -> Unit) {
    TarjetaSeccion(titulo = "2. Seleccionar Guardia / Observador") {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
            contentPadding = PaddingValues(vertical = Dimens.espacioChico)
        ) {
            items(observadores, key = { "${it.id}_${it.correo}" }) { obs ->
                val seleccionado = observadorSeleccionado?.id == obs.id
                val avatarBitmap = remember(obs.avatar) { ConvertidorImagen.aBitmap(obs.avatar) }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onObservadorCambia(obs) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (seleccionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(if (seleccionado) 3.dp else 1.dp, if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap.asImageBitmap(),
                                contentDescription = obs.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_persona),
                                contentDescription = null,
                                tint = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = obs.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BloqueHorarioSection(
    tramosHorarios: List<Pair<String, String>>,
    diasOffset: Int,
    tramoSeleccionado: Pair<String, String>,
    onDiasOffsetCambia: (Int) -> Unit,
    onTramoCambia: (Pair<String, String>) -> Unit
) {
    var expandidoHorario by remember { mutableStateOf(false) }

    TarjetaSeccion(titulo = "3. Bloque Horario de 30 Minutos") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            FilterChip(
                selected = diasOffset == 0,
                onClick = { onDiasOffsetCambia(0) },
                label = { Text("Hoy") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = diasOffset == 1,
                onClick = { onDiasOffsetCambia(1) },
                label = { Text("Mañana") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(Dimens.espacioChico))

        Text("Selecciona el tramo horario:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

        ExposedDropdownMenuBox(
            expanded = expandidoHorario,
            onExpandedChange = { expandidoHorario = it }
        ) {
            OutlinedTextField(
                value = "${tramoSeleccionado.first} a ${tramoSeleccionado.second} (30 min)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Bloque Horario") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoHorario) },
                shape = EstilosCampo.forma,
                colors = EstilosCampo.colores(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expandidoHorario,
                onDismissRequest = { expandidoHorario = false }
            ) {
                tramosHorarios.forEach { tramo ->
                    DropdownMenuItem(
                        text = { Text("${tramo.first} a ${tramo.second}") },
                        onClick = {
                            onTramoCambia(tramo)
                            expandidoHorario = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenTurnoCard(
    observadorSeleccionado: Usuario?,
    accesosInfo: List<Pair<Int, String>>,
    accesoSeleccionado: Int,
    fechaCalculada: String,
    tramoSeleccionado: Pair<String, String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Resumen del Turno", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Text(
                text = "${observadorSeleccionado?.nombre ?: "Sin seleccionar"} ➔ ${accesosInfo.find { it.first == accesoSeleccionado }?.second}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fecha: $fechaCalculada | Horario: ${tramoSeleccionado.first} a ${tramoSeleccionado.second}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProgramarScreenPreview() {
    AppConteoTheme {
        ProgramarContent(
            observadores = listOf(
                Usuario(id = 1, nombre = "Camila Rojas", correo = "camila@duocuc.cl"),
                Usuario(id = 2, nombre = "Diego Muñoz", correo = "diego@duocuc.cl")
            ),
            uiState = ProgramarUiState(),
            onAccesoCambia = {},
            onObservadorCambia = {},
            onDiasOffsetCambia = {},
            onTramoCambia = {},
            onProgramar = {},
            onVolver = {}
        )
    }
}
