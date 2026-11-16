package com.example.appconteo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onAnularRegistro: ((Long) -> Unit)? = null,
    onModificarRegistro: ((id: Long, acceso: Int, grupo: String, edad: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var fechaFiltro by remember { mutableStateOf("") }
    var accesoFiltro by remember { mutableStateOf<Int?>(null) }
    var tipoFiltro by remember { mutableStateOf<String?>(null) }

    var registroParaEditar by remember { mutableStateOf<HistorialRegistros?>(null) }
    var registroParaEliminar by remember { mutableStateOf<HistorialRegistros?>(null) }

    val accesosInfo = remember { CatalogosRincon.accesosInfo }

    val esAdmin = usuarioActual?.rolId == Rol.ID_ADMINISTRADOR
    val idActual = usuarioActual?.id ?: -1L
    val nombreActual = usuarioActual?.nombre ?: ""

    val filtrados = remember(historial, turnoIdFiltro, fechaFiltro, accesoFiltro, tipoFiltro, esAdmin, idActual, nombreActual) {
        historial.filter { item ->
            val coincideRol = esAdmin || item.usuarioId == idActual || (nombreActual.isNotEmpty() && item.observadorNombre.equals(nombreActual, ignoreCase = true))
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
                title = { Text(if (turnoIdFiltro != null && turnoIdFiltro > 0L) "Registros de este Turno" else "Registros de Aforo", fontWeight = FontWeight.Bold) },
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
                            text = "No hay registros para mostrar",
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

                            if (esAdmin) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (onModificarRegistro != null) {
                                        IconButton(onClick = { registroParaEditar = item }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_editar),
                                                contentDescription = "Editar Registro",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    if (onAnularRegistro != null) {
                                        IconButton(onClick = { registroParaEliminar = item }) {
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
    }

    // Reutilizamos el RegistroAforoDialog existente precargado con los datos actuales
    registroParaEditar?.let { reg ->
        RegistroAforoDialog(
            initialTipoGrupo = reg.tipoGrupo,
            initialRangoEdad = reg.rangoEtario,
            mostrarBotonSeguir = false,
            onDismiss = { registroParaEditar = null },
            onGuardarYCerrar = { tipoGrupo, rangoEdad ->
                onModificarRegistro?.invoke(reg.id, reg.acceso, tipoGrupo, rangoEdad)
                registroParaEditar = null
                Toast.makeText(context, "Registro actualizado con éxito", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Diálogo de Confirmación para Eliminar Registro
    registroParaEliminar?.let { reg ->
        AlertDialog(
            onDismissRequest = { registroParaEliminar = null },
            title = { Text("Anular Registro") },
            text = { Text("¿Estás seguro de que deseas anular este registro de ${reg.observadorNombre} (${reg.tipoRegistro})?") },
            confirmButton = {
                TextButton(onClick = {
                    onAnularRegistro?.invoke(reg.id)
                    registroParaEliminar = null
                    Toast.makeText(context, "Registro anulado con éxito", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Anular / Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { registroParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VerRegistrosPreview() {
    AppConteoTheme {
        VerRegistrosScreen(
            historial = listOf(
                HistorialRegistros(id = 1, observadorNombre = "Carlos", tipoRegistro = "Cliente (Boleta)", acceso = 1, idTurno = 10, tipoGrupo = "Solo", rangoEtario = "25-34", fecha = "2025-03-30", hora = "12:35"),
                HistorialRegistros(id = 2, observadorNombre = "Valentina", tipoRegistro = "Transeúnte (Aforo)", acceso = 2, idTurno = 10, tipoGrupo = "Grupo (3+)", rangoEtario = "18-24", fecha = "2025-03-30", hora = "11:00")
            ),
            turnoIdFiltro = 10L,
            onVolver = {}
        )
    }
}
