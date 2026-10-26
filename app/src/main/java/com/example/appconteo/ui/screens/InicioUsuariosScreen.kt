package com.example.appconteo.ui.screens

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.R
import com.example.appconteo.data.ConvertidorImagen
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Usuario
import com.example.appconteo.permisos.Permisos
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.FotoPerfil
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.ConteoViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel
import kotlinx.coroutines.launch

@Composable
fun InicioUsuariosScreen(
    usuario: Usuario?,
    usuariosViewModel: UsuariosViewModel = viewModel(),
    conteoViewModel: ConteoViewModel = viewModel(),
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val historial by usuariosViewModel.historial.collectAsStateWithLifecycle()

    InicioUsuariosContent(
        usuario = usuario,
        historial = historial,
        onRegistrarEnHistorial = { observador, tipo, detalle ->
            usuariosViewModel.registrarEnHistorial(observador, tipo, detalle)
            if (tipo.contains("Cliente", ignoreCase = true)) {
                conteoViewModel.escanearBoleta()
            }

        },
        onVerPerfil = onVerPerfil,
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUsuariosContent(
    usuario: Usuario?,
    historial: List<HistorialRegistros>,
    onRegistrarEnHistorial: (observadorNombre: String, tipo: String, detalle: String) -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoAforo by remember { mutableStateOf(false) }
    var esConBoleta by remember { mutableStateOf(false) }
    val nombreObservador = usuario?.nombre ?: "Observador"
    val fotoBitmap = remember(usuario?.avatar) {
        ConvertidorImagen.aBitmap(usuario?.avatar)
    }

    val boletasEscaneadas = remember(historial, nombreObservador) {
        historial.count {
            it.tipoRegistro.contains("Cliente", ignoreCase = true) &&
                    it.observadorNombre.equals(nombreObservador, ignoreCase = true)
        }
    }

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val res = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                Permisos.abrirAjustes(context)
            }
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) {
            esConBoleta = true
            mostrarDialogoAforo = true
        }
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No se encontró app de cámara") }
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else avisarPermisoDenegado("Se requiere permiso de cámara")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(R.string.saludo, nombreObservador),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_mi_perfil)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onVerPerfil()
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
                    title = { Text("Inicio", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = "Menú"
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioGrande),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    FotoPerfil(
                        fotoBitmap = fotoBitmap,
                        modifier = Modifier.size(70.dp)
                    )

                    Text(
                        text = nombreObservador,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(Dimens.espacioMedio))

                Image(
                    painter = painterResource(R.drawable.ic_imagen_boleta),
                    contentDescription = "Ilustración de boleta",
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Boletas Escaneadas: $boletasEscaneadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(Dimens.espacioGrande))

                BotonPrincipal(
                    texto = "Código De Barras",
                    onClick = {
                        if (Permisos.tiene(context, Permisos.CAMARA)) {
                            abrirCamara()
                        } else {
                            pedirPermisoCamara.launch(Permisos.CAMARA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonPrincipal(
                    texto = "Registro Rápido",
                    onClick = {
                        esConBoleta = false
                        mostrarDialogoAforo = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (mostrarDialogoAforo) {
                    RegistroAforoDialog(
                        onDismiss = { mostrarDialogoAforo = false },
                        onGuardar = { tipo, rangoEdad ->
                            if (esConBoleta) {
                                onRegistrarEnHistorial(
                                    nombreObservador,
                                    "Cliente",
                                    "Tipo: $tipo, Edad: $rangoEdad"
                                )
                                mostrarDialogoAforo = false
                                abrirCamara()
                            } else {
                                onRegistrarEnHistorial(
                                    nombreObservador,
                                    "Transeúnte",
                                    "Tipo: $tipo, Edad: $rangoEdad"
                                )
                                mostrarDialogoAforo = true
                            }
                        },
                        onGuardarYCerrar = { tipo, rangoEdad ->
                            val tipoRegistro = if (esConBoleta) "Cliente" else "Transeúnte"
                            onRegistrarEnHistorial(
                                nombreObservador,
                                tipoRegistro,
                                "Tipo: $tipo, Edad: $rangoEdad"
                            )
                            mostrarDialogoAforo = false
                            esConBoleta = false
                        }
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioUsuariosPreview() {
    AppConteoTheme {
        InicioUsuariosContent(
            usuario = Usuario(nombre = "Benjamin Curiñanco", correo = "benjamin@duocuc.cl"),
            historial = emptyList(),
            onRegistrarEnHistorial = { _, _, _ -> },
            onVerPerfil = {},
            onCerrarSesion = {}
        )
    }
}