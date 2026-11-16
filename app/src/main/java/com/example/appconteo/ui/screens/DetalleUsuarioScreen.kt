package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonVolver
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleUsuarioScreen(
    usuarioId: Long,
    usuariosViewModel: UsuariosViewModel = viewModel(),
    onVolver: () -> Unit
) {
    val usuario = remember(usuarioId) { usuariosViewModel.buscarPorId(usuarioId) }
    val nombreUsuario = usuario?.nombre ?: "Usuario"

    var fechaFiltro by remember { mutableStateOf("") }
    var accesoFiltro by remember { mutableStateOf<Int?>(null) }
    var limiteActual by remember { mutableStateOf(15) }

    val accesosInfo = remember { CatalogosRincon.accesosInfo }

    val registros = remember(usuarioId, fechaFiltro, limiteActual) {
        usuariosViewModel.listarHistorialPorUsuarioPaginado(
            usuarioId = usuarioId,
            fechaFiltro = fechaFiltro,
            limite = limiteActual
        )
    }

    val filtrados = remember(registros, accesoFiltro) {
        if (accesoFiltro == null) {
            registros
        } else {
            registros.filter { it.acceso == accesoFiltro }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registros de $nombreUsuario", fontWeight = FontWeight.Bold) },
                navigationIcon = { BotonVolver(onVolver = onVolver) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            CampoTexto(
                icono = R.drawable.ic_buscar,
                valor = fechaFiltro,
                onValorCambia = { fechaFiltro = it },
                etiqueta = "Filtrar por fecha (ej: 2025-03-30)",
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Filtrar por Punto de Acceso:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    item {
                        FilterChip(
                            selected = accesoFiltro == null,
                            onClick = { accesoFiltro = null },
                            label = { Text("Todos") }
                        )
                    }
                    items(accesosInfo) { (idAcceso, nombre) ->
                        val seleccionado = accesoFiltro == idAcceso
                        FilterChip(
                            selected = seleccionado,
                            onClick = { accesoFiltro = if (seleccionado) null else idAcceso },
                            label = { Text("A$idAcceso $nombre", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros con los filtros seleccionados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
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
                            }
                        }
                    }

                    if (filtrados.size >= limiteActual) {
                        item {
                            Spacer(Modifier.height(Dimens.espacioChico))
                            BotonPrincipal(
                                texto = "Mostrar 15 registros más",
                                onClick = { limiteActual += 15 },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(Dimens.espacioGrande))
                        }
                    }
                }
            }
        }
    }
}
