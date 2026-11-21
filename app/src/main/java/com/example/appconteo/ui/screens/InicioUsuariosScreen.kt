package com.example.appconteo.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.model.Clasificacion
import com.example.appconteo.model.ConteoUiState
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import com.example.appconteo.permisos.Permisos
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.FotoPerfil
import com.example.appconteo.ui.components.TarjetaMetrica
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.ConteoViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel
import kotlinx.coroutines.launch

@Composable
fun InicioUsuariosScreen(
    usuario: Usuario?,
    historial: List<HistorialRegistros>,
    conteoState: ConteoUiState,
    turnoActualServidor: Turno? = null,   // cambia cuando el servidor informa otro turno activo
    onObtenerTurnoActual: (Long) -> Turno?,
    onRegistrarEnHistorial: (String, String, Int, Long, Long, String, String, String?) -> Boolean,
    onRecargarConteo: () -> Unit,
    onValidarBoleta: (String) -> Boolean,
    onIntentarEscanear: () -> Boolean,
    onLimpiarMensajeConteo: () -> Unit,
    codigoScanned: String? = null,
    onCodigoConsumido: () -> Unit = {},
    onVerPerfil: () -> Unit,
    onVerTurnos: () -> Unit,
    onVerRegistros: () -> Unit,
    onEscanearQr: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    var codigoActual by rememberSaveable { mutableStateOf<String?>(null) }

    val turnoActual = remember(usuario, turnoActualServidor) {
        onObtenerTurnoActual(usuario?.id ?: 0)
    }
    val accesoActual = turnoActual?.acceso ?: -1

    var mostrarDialogoPorEscaneo by remember { mutableStateOf(false) }

    LaunchedEffect(conteoState.mensajeError) {
        conteoState.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            onLimpiarMensajeConteo()
        }
    }

    LaunchedEffect(codigoScanned) {
        if (!codigoScanned.isNullOrBlank()) {
            onCodigoConsumido()
            if (onValidarBoleta(codigoScanned)) {
                codigoActual = codigoScanned
                mostrarDialogoPorEscaneo = true
            }
        }
    }

    InicioUsuariosContent(
        usuario = usuario,
        historial = historial,
        accesoActual = accesoActual,
        codigoScannedTrigger = mostrarDialogoPorEscaneo,
        onDispararDialogoEscaneo = { mostrarDialogoPorEscaneo = false },
        onRegistrarEnHistorial = { observador, tipo, tipoGrupo, rangoEtario ->
            val esCliente = tipo == Clasificacion.CLIENTE.etiqueta
            val turnoAhora = onObtenerTurnoActual(usuario?.id ?: 0)
            val guardado = onRegistrarEnHistorial(
                observador,
                tipo,
                turnoAhora?.acceso ?: 1,
                turnoAhora?.id ?: 0L,
                usuario?.id ?: 0L,
                tipoGrupo,
                rangoEtario,
                if (esCliente) codigoActual else null
            )
            if (guardado) {
                codigoActual = null
                onRecargarConteo()
            }
            guardado
        },
        onVerPerfil = onVerPerfil,
        onVerTurnos = onVerTurnos,
        onVerRegistros = onVerRegistros,
        onEscanearQr = {
            if (onIntentarEscanear()) {
                onEscanearQr()
            }
        },
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioUsuariosContent(
    usuario: Usuario?,
    historial: List<HistorialRegistros>,
    accesoActual: Int = -1,
    codigoScannedTrigger: Boolean = false,
    onDispararDialogoEscaneo: () -> Unit = {},
    onRegistrarEnHistorial: (observadorNombre: String, tipo: String, tipoGrupo: String, rangoEtario: String) -> Boolean,
    onVerPerfil: () -> Unit,
    onVerTurnos: () -> Unit,
    onVerRegistros: () -> Unit,
    onEscanearQr: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoAforo by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var esConBoleta by remember { mutableStateOf(false) }
    val nombreObservador = usuario?.nombre ?: "Observador"

    val tieneTurnoActivo = accesoActual != -1
    val boletasEscaneadas = remember(historial, usuario) {
        val hoy = Fechas.hoy()
        val idUsu = usuario?.id ?: -1L
        historial.count {
            it.fecha == hoy &&
                    it.tipoRegistro == Clasificacion.CLIENTE.etiqueta &&
                    it.usuarioId == idUsu
        }
    }

    val accesosNombres = remember { CatalogosRincon.accesosInfo.toMap() }
    val nombrePuerta = accesosNombres[accesoActual] ?: "Sin asignar"

    LaunchedEffect(codigoScannedTrigger) {
        if (codigoScannedTrigger) {
            esConBoleta = true
            mostrarDialogoAforo = true
            onDispararDialogoEscaneo()
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

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) onEscanearQr() else avisarPermisoDenegado("Se requiere permiso de cámara")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            InicioUsuariosDrawer(
                nombre = nombreObservador,
                onVerTurnos = { scope.launch { drawerState.close() }; onVerTurnos() },
                onVerRegistros = { scope.launch { drawerState.close() }; onVerRegistros() },
                onVerPerfil = { scope.launch { drawerState.close() }; onVerPerfil() },
                onCerrarSesion = { scope.launch { drawerState.close() }; mostrarDialogoCerrarSesion = true }
            )
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
                InicioUsuariosTopBar(onAbrirMenu = { scope.launch { drawerState.open() } })
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
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TurnoActivoCard(
                    nombre = nombreObservador,
                    fotoUrl = usuario?.avatarUrl,
                    tieneTurnoActivo = tieneTurnoActivo,
                    nombrePuerta = nombrePuerta
                )

                Image(
                    painter = painterResource(R.drawable.ic_imagen_boleta),
                    contentDescription = "Ilustración de boleta",
                    modifier = Modifier.height(140.dp).fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                TarjetaMetrica(
                    titulo = "Boletas Escaneadas Hoy",
                    valor = "$boletasEscaneadas",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(Dimens.espacioChico))

                BotonPrincipal(
                    texto = "Código De Barras",
                    onClick = {
                        if (!tieneTurnoActivo) {
                            Toast.makeText(context, "⚠️ No tienes un turno activo asignado para este horario.", Toast.LENGTH_LONG).show()
                            return@BotonPrincipal
                        }
                        if (Permisos.tiene(context, Permisos.CAMARA)) {
                            onEscanearQr()
                        } else {
                            pedirPermisoCamara.launch(Permisos.CAMARA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonPrincipal(
                    texto = "Registro Rápido",
                    onClick = {
                        if (!tieneTurnoActivo) {
                            Toast.makeText(context, "⚠️ No tienes un turno activo asignado para este horario.", Toast.LENGTH_LONG).show()
                            return@BotonPrincipal
                        }
                        esConBoleta = false
                        mostrarDialogoAforo = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (mostrarDialogoAforo) {
                    RegistroAforoDialog(
                        onDismiss = { mostrarDialogoAforo = false },
                        onGuardar = { tipoGrupo, rangoEdad ->
                            val tipoReg = if (esConBoleta) Clasificacion.CLIENTE.etiqueta else Clasificacion.TRANSITO.etiqueta
                            val guardado = onRegistrarEnHistorial(nombreObservador, tipoReg, tipoGrupo, rangoEdad)
                            if (guardado) {
                                Toast.makeText(context, "¡Registro guardado con éxito!", Toast.LENGTH_SHORT).show()
                                if (esConBoleta) {
                                    mostrarDialogoAforo = false
                                    onEscanearQr()
                                }
                            }
                        },
                        onGuardarYCerrar = { tipoGrupo, rangoEdad ->
                            val tipoReg = if (esConBoleta) Clasificacion.CLIENTE.etiqueta else Clasificacion.TRANSITO.etiqueta
                            val guardado = onRegistrarEnHistorial(nombreObservador, tipoReg, tipoGrupo, rangoEdad)
                            if (guardado) {
                                Toast.makeText(context, "¡Registro guardado con éxito!", Toast.LENGTH_SHORT).show()
                                mostrarDialogoAforo = false
                                esConBoleta = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InicioUsuariosDrawer(
    nombre: String,
    onVerTurnos: () -> Unit,
    onVerRegistros: () -> Unit,
    onVerPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.saludo, nombre),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(Dimens.espacioMedio)
        )
        HorizontalDivider()
        NavigationDrawerItem(label = { Text("Mis Turnos") }, selected = false, onClick = onVerTurnos)
        NavigationDrawerItem(label = { Text("Ver Registros") }, selected = false, onClick = onVerRegistros)
        NavigationDrawerItem(label = { Text(stringResource(R.string.menu_mi_perfil)) }, selected = false, onClick = onVerPerfil)
        NavigationDrawerItem(label = { Text(stringResource(R.string.menu_cerrar_sesion)) }, selected = false, onClick = onCerrarSesion)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InicioUsuariosTopBar(onAbrirMenu: () -> Unit) {
    TopAppBar(
        title = { Text("Panel de Control - Guardia", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onAbrirMenu) {
                Icon(painter = painterResource(R.drawable.ic_menu), contentDescription = "Menú")
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
private fun TurnoActivoCard(
    nombre: String,
    fotoUrl: String?,
    tieneTurnoActivo: Boolean,
    nombrePuerta: String
) {
    TarjetaSeccion(titulo = "Estado del Turno") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            FotoPerfil(fotoUrl = fotoUrl, modifier = Modifier.size(60.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (tieneTurnoActivo) {
                    Text(text = "Puerta Asignada: Acceso $nombrePuerta", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                } else {
                    Text(text = "⚠️ Sin turno activo en este horario", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
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
            accesoActual = 2,
            onRegistrarEnHistorial = { _, _, _, _ -> true },
            onVerPerfil = {},
            onVerTurnos = {},
            onVerRegistros = {},
            onEscanearQr = {},
            onCerrarSesion = {}
        )
    }
}
