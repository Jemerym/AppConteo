package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.model.Clasificacion
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerRegistrosScreen(
    historial: List<HistorialRegistros>,
    usuarioActual: Usuario? = null,
    turnoIdFiltro: Long? = null,
    onVolver: () -> Unit,
    onAnularRegistro: ((Long) -> Unit)? = null
) {
    var fechaFiltro by remember { mutableStateOf("") }
    var accesoFiltro by remember { mutableStateOf<Int?>(null) }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }

    val accesosInfo = remember { CatalogosRincon.accesosInfo }

    val esAdmin = usuarioActual?.rolId == Rol.ID_ADMINISTRADOR
    val idActual = usuarioActual?.id ?: -1L
    val nombreActual = usuarioActual?.nombre ?: ""

    val filtrados = remember(historial, turnoIdFiltro, fechaFiltro, accesoFiltro, tipoFiltro, esAdmin, idActual, nombreActual) {
        historial.filter { item ->
            val coincideRol = esAdmin || item.usuarioId == idActual
            val coincideTurno = turnoIdFiltro == null || turnoIdFiltro <= 0L || item.idTurno == turnoIdFiltro
            val coincideFecha = fechaFiltro.isBlank() || item.fecha.contains(fechaFiltro.trim())
            val coincideAcceso = accesoFiltro == null || item.acceso == accesoFiltro
            val coincideTipo = tipoFiltro.isNullOrBlank() || item.tipoRegistro.contains(tipoFiltro!!, ignoreCase = true)

            coincideRol && coincideTurno && coincideFecha && coincideAcceso && coincideTipo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (turnoIdFiltro != null && turnoIdFiltro > 0L) "Registros de este Turno" else "Mis Registros de Aforo", fontWeight = FontWeight.Bold) },
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
            contentPadding = PaddingValues(vertical = Dimens.espacioMedio)
        ) {
            if (turnoIdFiltro == null || turnoIdFiltro <= 0L) {
                item {
                    CampoTexto(
                        valor = fechaFiltro,
                        onValorCambia = { fechaFiltro = it },
                        etiqueta = "Buscar por fecha (ej: 2025-03-30)",
                        icono = R.drawable.ic_buscar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Acceso", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            accesosInfo.forEach { (idAcceso, nombre) ->
                                val seleccionado = accesoFiltro == idAcceso
                                FilterChip(
                                    selected = seleccionado,
                                    onClick = { accesoFiltro = if (seleccionado) null else idAcceso },
                                    label = { Text("A$idAcceso $nombre", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tipo de Flujo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        val esCliente = tipoFiltro == Clasificacion.CLIENTE.etiqueta
                        val esTransito = tipoFiltro == Clasificacion.TRANSITO.etiqueta

                        FilterChip(
                            selected = esCliente,
                            onClick = { tipoFiltro = if (esCliente) null else Clasificacion.CLIENTE.etiqueta },
                            label = { Text("Clientes") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = esTransito,
                            onClick = { tipoFiltro = if (esTransito) null else Clasificacion.TRANSITO.etiqueta },
                            label = { Text("Transeúntes") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (filtrados.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros tuyos para mostrar",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filtrados, key = { it.id }) { item ->
                    val accesosMap = remember { accesosInfo.toMap() }
                    val nombrePuerta = accesosMap[item.acceso] ?: "Norte"

                    TarjetaSeccion(
                        titulo = "${item.tipoRegistro}  •  A${item.acceso} $nombrePuerta",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.detalle} | Por: ${item.observadorNombre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Fecha: ${item.fecha}  |  Hora: ${item.hora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (esAdmin && onAnularRegistro != null) {
                                IconButton(onClick = { onAnularRegistro(item.id) }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_eliminar),
                                        contentDescription = "Anular Registro",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vista General Admin con Filtros")
@Composable
private fun VerRegistrosPreview() {
    AppConteoTheme {
        VerRegistrosScreen(
            historial = listOf(
                HistorialRegistros(id = 1, observadorNombre = "Carlos Observador", tipoRegistro = "Cliente (Boleta)", acceso = 1, idTurno = 1, usuarioId = 2, tipoGrupo = "Solo", rangoEtario = "25-34", fecha = "2025-03-30", hora = "12:35"),
                HistorialRegistros(id = 2, observadorNombre = "Valentina Observadora", tipoRegistro = "Transeúnte (Aforo)", acceso = 2, idTurno = 2, usuarioId = 3, tipoGrupo = "Grupo (3+)", rangoEtario = "18-24", fecha = "2025-03-30", hora = "11:00")
            ),
            usuarioActual = Usuario(id = 1, nombre = "Admin", correo = "admin@demo.cl", rolId = 1),
            turnoIdFiltro = null,
            onVolver = {}
        )
    }
}
