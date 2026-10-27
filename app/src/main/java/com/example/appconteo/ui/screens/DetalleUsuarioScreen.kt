package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*import com.example.appconteo.R

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonVolver
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleUsuarioScreen(
    usuarioId: Long,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { UsuarioDbHelper(context) }

    val usuario = remember(usuarioId) { db.buscarPorId(usuarioId) }
    val nombreUsuario = usuario?.nombre ?: "Usuario"

    var fechaFiltro by remember { mutableStateOf("") }
    var limiteActual by remember { mutableStateOf(15) }

    val registros = remember(nombreUsuario, fechaFiltro, limiteActual) {
        db.listarHistorialPorObservadorPaginado(
            nombreObservador = nombreUsuario,
            fechaFiltro = fechaFiltro,
            limite = limiteActual
        )
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
                icono = R.drawable.ic_menu,
                valor = fechaFiltro,
                onValorCambia = { fechaFiltro = it },
                etiqueta = "Filtrar por fecha (ej: 2024-05-20)",
                modifier = Modifier.fillMaxWidth()
            )

            if (registros.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros para este usuario",
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
                    items(registros, key = { it.id }) { item ->
                        TarjetaSeccion(
                            titulo = item.tipoRegistro,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.detalle}\nFecha: ${item.fecha} - ${item.hora}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (registros.size >= limiteActual) {
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