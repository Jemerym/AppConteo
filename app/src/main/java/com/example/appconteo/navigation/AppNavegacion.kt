package com.example.appconteo.navigation

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.screens.AdminScreen
import com.example.appconteo.ui.screens.EditarPerfilScreen
import com.example.appconteo.ui.screens.EscanearQrScreen
import com.example.appconteo.ui.screens.InicioUsuariosScreen
import com.example.appconteo.ui.screens.LoginScreen
import com.example.appconteo.ui.screens.MisTurnosScreen
import com.example.appconteo.ui.screens.PerfilScreen
import com.example.appconteo.ui.screens.ProgramarScreen
import com.example.appconteo.ui.screens.RecuperarClaveScreen
import com.example.appconteo.ui.screens.RegistroScreen
import com.example.appconteo.ui.screens.UsuariosScreen
import com.example.appconteo.ui.screens.VerRegistrosScreen
import com.example.appconteo.viewmodel.ConteoViewModel
import com.example.appconteo.ui.components.ActualizarPeriodicamente
import com.example.appconteo.viewmodel.TurnosViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel
import com.example.appconteo.ui.theme.TemaApp

@Composable
fun AppNavegacion(
    usuariosViewModel: UsuariosViewModel = viewModel(),
    conteoViewModel: ConteoViewModel = viewModel(),
    turnosViewModel: TurnosViewModel = viewModel(),
    temaActual: TemaApp = TemaApp.CLARO,
    onCambiarTema: (TemaApp) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val historial by usuariosViewModel.historial.collectAsStateWithLifecycle()
    val turnos by turnosViewModel.turnos.collectAsStateWithLifecycle()
    val turnoActualServidor by turnosViewModel.turnoActual.collectAsStateWithLifecycle()
    val mensaje by usuariosViewModel.mensaje.collectAsStateWithLifecycle()


    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            usuariosViewModel.limpiarMensaje()
        }
    }

    // El conteo se recalcula cuando llega un registro nuevo (o se anula/restaura) desde registros-service
    LaunchedEffect(historial) {
        conteoViewModel.recargarConteo()
    }

    val mensajeTurnos by turnosViewModel.mensaje.collectAsStateWithLifecycle()
    LaunchedEffect(mensajeTurnos) {
        mensajeTurnos?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            turnosViewModel.limpiarMensaje()
        }
    }

    // Cada vez que cambia el usuario (login, sesión restaurada, cierre de sesión) se cargan sus turnos
    LaunchedEffect(usuarioActual) {
        turnosViewModel.establecerUsuario(usuarioActual)

        // Si la sesión se cerró desde el ViewModel (por ejemplo, el usuario fue anulado mientras
        // estaba conectado) se vuelve al Login. Antes quedaba en una pantalla sin sesión.
        val rutaActual = navController.currentDestination?.route
        val rutasPublicas = setOf(Ruta.Login.ruta, Ruta.Registro.ruta, Ruta.RecuperarClave.ruta)
        if (usuarioActual == null && rutaActual != null && rutaActual !in rutasPublicas) {
            navController.cerrarSesionLimpiando()
        }
    }

    val startDestination = remember {
        when {
            usuariosViewModel.usuarioActual.value == null -> Ruta.Login.ruta
            usuariosViewModel.esAdministrador -> Ruta.Admin.ruta
            else -> Ruta.InicioUsuarios.ruta
        }
    }

    val cerrarSesion: () -> Unit = {
        navController.cerrarSesionLimpiando()
        usuariosViewModel.cerrarSesion()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut() }
    ) {

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onGuardarUsuario = usuariosViewModel::registrar,
                onRegistroExitoso = { _ ->
                    navController.popBackStack(Ruta.Login.ruta, inclusive = false)
                },
                onIrLogin = { navController.popBackStack(Ruta.Login.ruta, inclusive = false) }
            )
        }

        composable(Ruta.Login.ruta) { backStackEntry ->
            // Bandera que deja puesta Recuperar clave cuando termina bien
            val limpiarCampos by backStackEntry.savedStateHandle
                .getStateFlow("limpiar_login", false)
                .collectAsStateWithLifecycle()

            LoginScreen(
                onIniciarSesion = usuariosViewModel::iniciarSesion,
                onLoginExitoso = {
                    val destino = if (usuariosViewModel.esAdministrador) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onIrRegistro = {
                    navController.navegarUnico(Ruta.Registro.ruta)
                },
                onIrRecuperarClave = {
                    navController.navegarUnico(Ruta.RecuperarClave.ruta)
                },
                limpiarCampos = limpiarCampos,
                onCamposLimpiados = { backStackEntry.savedStateHandle["limpiar_login"] = false }
            )
        }

        composable(Ruta.RecuperarClave.ruta) {
            RecuperarClaveScreen(
                onRestablecerClave = usuariosViewModel::restablecerClave,
                onVolverLogin = { navController.popBackStack() },
                onRecuperacionExitosa = {
                    // Avisa al Login que limpie sus campos y vuelve a él
                    navController.previousBackStackEntry?.savedStateHandle?.set("limpiar_login", true)
                    navController.popBackStack()
                }
            )
        }

        composable(Ruta.Admin.ruta) {
            SoloAdmin(usuario = usuarioActual, onDenegado = { navController.popBackStack() }) { admin ->
                val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()

                // Mientras el admin está en esta pantalla, pide los registros al servidor cada 15 s
                ActualizarPeriodicamente { usuariosViewModel.refrescarHistorial() }

                AdminScreen(
                    historial = historial,
                    usuarioActual = admin,
                    conteoState = conteoState,
                    onRecargarConteo = conteoViewModel::recargarConteo,
                    onVerPerfil = { navController.navegarUnico(Ruta.Perfil.ruta) },
                    onVerUsuarios = { navController.navegarUnico(Ruta.Usuarios.ruta) },
                    onVerRegistros = { navController.navegarUnico(Ruta.VerRegistros.crear()) },
                    onVerTurnos = { navController.navegarUnico(Ruta.MisTurnos.ruta) },
                    onIrProgramarSesion = { navController.navegarUnico(Ruta.ProgramarSesion.crear()) },
                    onCerrarSesion = cerrarSesion
                )
            }
        }
        composable(
            route = Ruta.VerRegistros.ruta,
            arguments = listOf(
                navArgument("turnoId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("usuarioId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val turnoId = backStackEntry.arguments?.getLong("turnoId") ?: 0L
            val usuarioId = backStackEntry.arguments?.getLong("usuarioId") ?: 0L
            ActualizarPeriodicamente { usuariosViewModel.refrescarHistorial() }
            if (usuarioActual?.esAdmin == false) {
                ActualizarPeriodicamente { turnosViewModel.actualizarTurnoActual() }
            }

            VerRegistrosScreen(
                historial = historial,
                usuarioActual = usuarioActual,
                turnoIdFiltro = if (turnoId > 0L) turnoId else null,
                usuarioIdFiltro = if (usuarioId > 0L) usuarioId else null,
                tituloUsuario = if (usuarioId > 0L) usuarios.find { it.id == usuarioId }?.nombre else null,
                onAnularRegistro = { id -> usuariosViewModel.anularHistorial(id, turnoActualServidor) },
                onModificarRegistro = { id, acceso, grupo, edad ->
                    usuariosViewModel.actualizarHistorial(id, acceso, grupo, edad, turnoActualServidor)
                },
                onRestaurarRegistro = { id -> usuariosViewModel.restaurarHistorial(id, turnoActualServidor) },
                puedeEditar = { registro -> usuariosViewModel.puedeEditarRegistro(registro, turnoActualServidor) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.InicioUsuarios.ruta) { backStackEntry ->
            val codigoScanned = backStackEntry.savedStateHandle.get<String>("codigo_scanned")
            val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()


            ActualizarPeriodicamente { turnosViewModel.actualizarTurnoActual() }

            InicioUsuariosScreen(
                usuario = usuarioActual,
                historial = historial,
                conteoState = conteoState,
                turnoActualServidor = turnoActualServidor,
                onObtenerTurnoActual = turnosViewModel::obtenerTurnoActual,
                onRegistrarEnHistorial = usuariosViewModel::registrarEnHistorial,
                onRecargarConteo = conteoViewModel::recargarConteo,
                onValidarBoleta = conteoViewModel::validarBoleta,
                onIntentarEscanear = conteoViewModel::intentarEscanear,
                onLimpiarMensajeConteo = conteoViewModel::limpiarMensaje,
                codigoScanned = codigoScanned,
                onCodigoConsumido = { backStackEntry.savedStateHandle.remove<String>("codigo_scanned") },
                onVerPerfil = { navController.navegarUnico(Ruta.Perfil.ruta) },
                onVerTurnos = { navController.navegarUnico(Ruta.MisTurnos.ruta) },
                onVerRegistros = { navController.navegarUnico(Ruta.VerRegistros.crear()) },
                onEscanearQr = { navController.navegarUnico(Ruta.EscanearQr.ruta) },
                onCerrarSesion = cerrarSesion
            )
        }

        composable(Ruta.MisTurnos.ruta) {
            // TIEMPO REAL: mientras "Mis turnos" está abierta, se vuelve a pedir la lista al servidor cada 15 s
            // (aparecen los turnos nuevos o modificados sin salir y volver a entrar).
            ActualizarPeriodicamente { turnosViewModel.refrescarEnSegundoPlano() }

            MisTurnosScreen(
                usuario = usuarioActual,
                turnos = turnos,
                onAnularTurno = turnosViewModel::anular,
                onRestaurarTurno = turnosViewModel::restaurar,
                onRecargarTurnos = turnosViewModel::recargar,
                onVerRegistrosTurno = { turnoId ->
                    navController.navegarUnico(Ruta.VerRegistros.crear(turnoId))
                },
                onEditarTurno = { turnoId ->
                    navController.navegarUnico(Ruta.ProgramarSesion.crear(turnoId))
                },
                onVolver = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Ruta.EscanearQr.ruta) {
            EscanearQrScreen(
                onCodigoDetectado = { codigo: String ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("codigo_scanned", codigo)
                    navController.popBackStack()
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.Usuarios.ruta) {
            SoloAdmin(usuario = usuarioActual, onDenegado = { navController.popBackStack() }) { admin ->
                UsuariosScreen(
                    usuarios = usuarios,
                    usuarioActual = admin,
                    filtro = filtro,
                    onBuscar = usuariosViewModel::buscar,
                    onVerRegistros = { id -> navController.navegarUnico(Ruta.VerRegistros.crear(usuarioId = id)) },
                    onAnularUsuario = usuariosViewModel::anular,
                    onRestaurarUsuario = usuariosViewModel::restaurar,
                    onVolver = {
                        // Al salir se limpia el buscador (como limpiar() en Login)
                        usuariosViewModel.limpiar()
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }

        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuariosViewModel = usuariosViewModel,
                temaActual = temaActual,
                onCambiarTema = onCambiarTema,
                onModificarPerfil = {
                    navController.navegarUnico(Ruta.EditarPerfil.ruta)
                },
                onVolver = {
                    usuariosViewModel.recargarUsuarioActual()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }



        composable(Ruta.EditarPerfil.ruta) {
            val usuario = usuarioActual
            if (usuario != null) {
                EditarPerfilScreen(
                    usuario = usuario,
                    onVolver = { navController.navigateUp() },
                    onGuardar = { cambios, onExito, onError ->
                        usuariosViewModel.actualizarPerfil(usuario, cambios, onExito, onError)
                    }
                )
            }
        }

        composable(
            route = Ruta.ProgramarSesion.ruta,
            arguments = listOf(navArgument("turnoId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val turnoId = backStackEntry.arguments?.getLong("turnoId") ?: 0L

            SoloAdmin(usuario = usuarioActual, onDenegado = { navController.popBackStack() }) { _ ->
                ProgramarScreen(
                    usuariosViewModel = usuariosViewModel,
                    turnosViewModel = turnosViewModel,
                    turnoId = turnoId,
                    onVolver = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        }
                    },
                    onSesionProgramada = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun SoloAdmin(
    usuario: Usuario?,
    onDenegado: () -> Unit,
    contenido: @Composable (Usuario) -> Unit
) {
    // Evita que la pantalla desaparezca mientras corre la animación de salida
    var ultimo by remember { mutableStateOf<Usuario?>(null) }
    if (usuario != null) ultimo = usuario
    val actual = usuario ?: ultimo

    when {
        actual == null -> Unit
        actual.esAdmin -> contenido(actual)
        else -> LaunchedEffect(Unit) { onDenegado() }
    }
}

private fun NavHostController.cerrarSesionLimpiando() {
    navigate(Ruta.Login.ruta) {
        popUpTo(0) { inclusive = true }
    }
}

private fun NavHostController.navegarUnico(ruta: String) {
    navigate(ruta) {
        launchSingleTop = true
    }
}
