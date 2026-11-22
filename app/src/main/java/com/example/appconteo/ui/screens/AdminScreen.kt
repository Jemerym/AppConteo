package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.model.Clasificacion
import com.example.appconteo.model.ConteoUiState
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.TarjetaAlerta
import com.example.appconteo.ui.components.TarjetaMetrica
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    historial: List<HistorialRegistros>,
    usuarioActual: Usuario?,
    conteoState: ConteoUiState,
    onRecargarConteo: () -> Unit,
    onVerPerfil: () -> Unit = {},
    onVerUsuarios: () -> Unit,
    onVerRegistros: () -> Unit = {},
    onVerTurnos: () -> Unit = {},
    onIrProgramarSesion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    LaunchedEffect(historial) {
        onRecargarConteo()
    }
    AdminContent(
        historial = historial.filter { !it.anulado },   // los anulados no cuentan (se ven en Ver Registros)
        usuarioActual = usuarioActual,
        conteoState = conteoState,
        onVerPerfil = onVerPerfil,
        onVerUsuarios = onVerUsuarios,
        onVerRegistros = onVerRegistros,
        onVerTurnos = onVerTurnos,
        onIrProgramarSesion = onIrProgramarSesion,
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContent(
    historial: List<HistorialRegistros>,
    usuarioActual: Usuario?,
    conteoState: ConteoUiState,
    onVerPerfil: () -> Unit = {},
    onVerUsuarios: () -> Unit,
    onVerRegistros: () -> Unit = {},
    onVerTurnos: () -> Unit = {},
    onIrProgramarSesion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    val clientesCount = conteoState.clientes
    val totalPersonas = conteoState.totalPersonas
    val transeuntesCount = conteoState.transeuntes
    val boletasFaltantes = conteoState.boletasFaltantes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.saludo, usuarioActual?.nombre ?: "Admin"),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerPerfil()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.titulo_usuarios)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerUsuarios()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Ver Turnos Programados") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerTurnos()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Ver Registros de Aforo") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerRegistros()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogoCerrarSesion = true
                    }
                )
            }
        }
    ) {
        if (mostrarDialogoCerrarSesion) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCerrarSesion = false },
                title = { Text("Cerrar Sesión") },
                text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarDialogoCerrarSesion = false
                        onCerrarSesion()
                    }) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel de Administración") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = stringResource(R.string.cd_menu)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onIrProgramarSesion,
                    icon = { Icon(painterResource(R.drawable.ic_check), contentDescription = null) },
                    text = { Text("Programar") }
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
                item {
                    TarjetaMetrica(
                        titulo = "Total ingresos",
                        valor = "$totalPersonas",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        colorValor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                    ) {
                        TarjetaMetrica(
                            titulo = "Clientes",
                            valor = "$clientesCount",
                            colorValor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaMetrica(
                            titulo = "Transeúntes",
                            valor = "$transeuntesCount",
                            colorValor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    TarjetaAlerta(mensaje = "Faltan $boletasFaltantes boletas por escanear")
                }

                item {
                    Text(
                        text = "Historial de Registros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (historial.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay registros de aforo aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(historial, key = { it.id }) { item ->
                        val accesosNombres = remember { CatalogosRincon.accesosInfo.toMap() }
                        val nombrePuerta = accesosNombres[item.acceso] ?: "Norte"

                        TarjetaSeccion(
                            titulo = "${item.tipoRegistro}  •  A${item.acceso} $nombrePuerta",
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.detalle} | Por: ${item.observadorNombre}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.hora,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminPreview() {
    AppConteoTheme {
        AdminContent(
            historial = listOf(
                HistorialRegistros(id = 1, observadorNombre = "Carlos Observador", tipoRegistro = Clasificacion.CLIENTE.etiqueta, acceso = 1, idTurno = 1, usuarioId = 2,
                    tipoGrupo = "Solo", rangoEtario = "25-34", fecha = "2025-03-30", hora = "12:35"
                ),
                HistorialRegistros(id = 2, observadorNombre = "Valentina Observadora", tipoRegistro = Clasificacion.TRANSITO.etiqueta, acceso = 2, idTurno = 2, usuarioId = 3,
                    tipoGrupo = "Grupo (3+)", rangoEtario = "18-24", fecha = "2025-03-30", hora = "11:00"
                )
            ),
            usuarioActual = Usuario(id = 1, nombre = "Admin", correo = "admin@demo.cl", rolNombre = "Administrador"),
            conteoState = ConteoUiState(totalPersonas = 100, boletasCajaTotal = 15),
            onVerPerfil = {},
            onVerUsuarios = {},
            onVerRegistros = {},
            onIrProgramarSesion = {},
            onCerrarSesion = {}
        )
    }
}
