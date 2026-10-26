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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.model.ConteoUiState
import com.example.appconteo.model.HistorialItem
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.TarjetaAlerta
import com.example.appconteo.ui.components.TarjetaMetrica
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.ConteoViewModel
import kotlinx.coroutines.launch
@Composable
fun AdminScreen(
    historial: List<HistorialItem>,
    usuarioActual: Usuario?,
    conteoViewModel: ConteoViewModel = viewModel(),
    onVerUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()
    val clientesContador = remember(historial) {
        historial.count { it.tipoRegistro.contains("Cliente", ignoreCase = true) }
    }
    LaunchedEffect(clientesContador) {
        conteoViewModel.actualizarClientes(clientesContador)
    }
    AdminContent(
        historial = historial,
        usuarioActual = usuarioActual,
        conteoState = conteoState,
        onVerUsuarios = onVerUsuarios,
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContent(
    historial: List<HistorialItem>,
    usuarioActual: Usuario?,
    conteoState: ConteoUiState,
    onVerUsuarios: () -> Unit,
    onCerrarSesion: () -> Unit
)  {    
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val clientesCount = remember(historial) { historial.count { it.tipoRegistro.contains("Cliente", ignoreCase = true) } }
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
                    label = { Text(stringResource(R.string.titulo_usuarios)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerUsuarios()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCerrarSesion()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (usuarioActual != null) stringResource(R.string.saludo, usuarioActual.nombre)
                            else "Panel de Administración"
                        )
                    },
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
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TarjetaMetrica(
                    titulo = "Total Personas Adentro",
                    valor = "$totalPersonas",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
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
                TarjetaAlerta(mensaje = "Faltan $boletasFaltantes boletas por escanear")
                Spacer(Modifier.height(Dimens.espacioChico))
                Text(
                    text = "Historial de Registros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                if (historial.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay registros de aforo aún",
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
                        items(historial, key = { it.id }) { item ->
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
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminPreview() {
    AppConteoTheme {
        AdminContent(
            historial = listOf(
                HistorialItem(1, "Carlos Observador", "Cliente (Boleta)", "Tipo: Solo, Edad: 25-34", "12:35"),
                HistorialItem(2, "Valentina Observadora", "Transeúnte (Aforo)", "Tipo: Grupo (3+), Edad: 18-24", "11:00")
            ),
            usuarioActual = Usuario(nombre = "Admin Principal", correo = "admin@demo.cl", rolNombre = "Administrador"),
            conteoState = ConteoUiState(totalPersonas = 100, boletasCajaTotal = 15),
            onVerUsuarios = {},
            onCerrarSesion = {}
        )
    }
}