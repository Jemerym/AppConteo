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
import com.example.appconteo.ui.screens.DetalleUsuarioScreen
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
import com.example.appconteo.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion(
    usuariosViewModel: UsuariosViewModel = viewModel(),
    conteoViewModel: ConteoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val historial by usuariosViewModel.historial.collectAsStateWithLifecycle()
    val turnos by usuariosViewModel.turnos.collectAsStateWithLifecycle()
    val mensaje by usuariosViewModel.mensaje.collectAsStateWithLifecycle()

    val esAdmin = usuariosViewModel.esAdministrador

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            usuariosViewModel.limpiarMensaje()
        }
    }

    val startDestination = if (usuarioActual != null) {
        if (esAdmin) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta
    } else {
        Ruta.Login.ruta
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
                onExisteCorreo = usuariosViewModel::existeCorreo,
                onGuardarUsuario = usuariosViewModel::insertarUsuario,
                onRegistroExitoso = { _ ->
                    navController.popBackStack(Ruta.Login.ruta, inclusive = false)
                },
                onIrLogin = { navController.popBackStack(Ruta.Login.ruta, inclusive = false) }
            )
        }

        composable(Ruta.Login.ruta) {
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
                }
            )
        }

        composable(Ruta.RecuperarClave.ruta) {
            RecuperarClaveScreen(
                onRestablecerClave = usuariosViewModel::recuperarClave,
                onVolverLogin = { navController.popBackStack() }
            )
        }

        composable(Ruta.Admin.ruta) {
            SoloAdmin(usuario = usuarioActual, onDenegado = { navController.popBackStack() }) { admin ->
                val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()
                AdminScreen(
                    historial = historial,
                    usuarioActual = admin,
                    conteoState = conteoState,
                    onRecargarConteo = conteoViewModel::recargarConteo,
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
            arguments = listOf(navArgument("turnoId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val turnoId = backStackEntry.arguments?.getLong("turnoId") ?: 0L
            VerRegistrosScreen(
                historial = historial,
                usuarioActual = usuarioActual,
                turnoIdFiltro = if (turnoId > 0L) turnoId else null,
                onAnularRegistro = usuariosViewModel::eliminarHistorial,
                onModificarRegistro = usuariosViewModel::actualizarHistorial,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Ruta.InicioUsuarios.ruta) { backStackEntry ->
            val codigoScanned = backStackEntry.savedStateHandle.get<String>("codigo_scanned")
            val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()

            InicioUsuariosScreen(
                usuario = usuarioActual,
                historial = historial,
                conteoState = conteoState,
                onObtenerTurnoActual = usuariosViewModel::obtenerTurnoActual,
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
            MisTurnosScreen(
                usuario = usuarioActual,
                turnos = turnos,
                onEliminarTurno = usuariosViewModel::eliminarTurno,
                onRecargarTurnos = usuariosViewModel::recargarTurnos,
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
                    onEditar = { id -> navController.navegarUnico(Ruta.DetalleUsuario.crear(id)) },
                    onEliminarUsuario = usuariosViewModel::eliminar,
                    onVolver = {
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

        composable(
            route = Ruta.DetalleUsuario.ruta,
            arguments = listOf(navArgument("usuarioId") { type = NavType.LongType })
        ) { entrada ->
            val usuarioId = entrada.arguments?.getLong("usuarioId") ?: 0L
            SoloAdmin(usuario = usuarioActual, onDenegado = { navController.popBackStack() }) { _ ->
                DetalleUsuarioScreen(
                    usuarioId = usuarioId,
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(Ruta.EditarPerfil.ruta) {
            val usuario = usuarioActual
            if (usuario != null) {
                EditarPerfilScreen(
                    usuario = usuario,
                    onVolver = { navController.navigateUp() },
                    onGuardar = { editado ->
                        usuariosViewModel.actualizar(editado)
                        navController.navigateUp()
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
