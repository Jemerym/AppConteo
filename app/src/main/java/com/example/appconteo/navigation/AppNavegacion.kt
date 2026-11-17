package com.example.appconteo.navigation

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val historial by usuariosViewModel.historial.collectAsStateWithLifecycle()
    val turnos by usuariosViewModel.turnos.collectAsStateWithLifecycle()
    val mensaje by usuariosViewModel.mensaje.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(mensaje) {
        mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            usuariosViewModel.limpiarMensaje()
        }
    }

    val startDestination = if (usuarioActual != null) {
        val esAdmin = usuariosViewModel.esAdministrador
        if (esAdmin) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta
    } else {
        Ruta.Login.ruta
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onExisteCorreo = usuariosViewModel::existeCorreo,
                onGuardarUsuario = usuariosViewModel::insertarUsuario,
                onRegistroExitoso = { usuario ->
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Registro.ruta) { inclusive = true }
                    }
                },
                onIrLogin = { navController.navigate(Ruta.Login.ruta) }
            )
        }

        composable(Ruta.Login.ruta) {
            LoginScreen(
                onIniciarSesion = usuariosViewModel::iniciarSesion,
                onLoginExitoso = {
                    val esAdmin = usuariosViewModel.esAdministrador
                    val destino = if (esAdmin) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta
                    navController.navigate(destino)
                },
                onIrRegistro = {
                    navController.navigate(Ruta.Registro.ruta) {
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                onIrRecuperarClave = {
                    navController.navigate(Ruta.RecuperarClave.ruta)
                }
            )
        }

        composable(Ruta.RecuperarClave.ruta) {
            RecuperarClaveScreen(
                onVerificarClave = usuariosViewModel::recuperarClave,
                onVolverLogin = { navController.popBackStack() }
            )
        }

        composable(Ruta.Admin.ruta) {
            val conteoState by conteoViewModel.uiState.collectAsStateWithLifecycle()
            AdminScreen(
                historial = historial,
                usuarioActual = usuarioActual,
                conteoState = conteoState,
                onRecargarConteo = conteoViewModel::recargarConteo,
                onVerUsuarios = { navController.navigate(Ruta.Usuarios.ruta) },
                onVerRegistros = { navController.navigate(Ruta.VerRegistros.crear()) },
                onVerTurnos = { navController.navigate(Ruta.MisTurnos.ruta) },
                onIrProgramarSesion = { navController.navigate(Ruta.ProgramarSesion.crear()) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                    usuariosViewModel.cerrarSesion()
                }
            )
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
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onVerTurnos = { navController.navigate(Ruta.MisTurnos.ruta) },
                onVerRegistros = { navController.navigate(Ruta.VerRegistros.crear()) },
                onEscanearQr = { navController.navigate(Ruta.EscanearQr.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                    usuariosViewModel.cerrarSesion()
                }
            )
        }

        composable(Ruta.MisTurnos.ruta) {
            MisTurnosScreen(
                usuario = usuarioActual,
                turnos = turnos,
                onEliminarTurno = usuariosViewModel::eliminarTurno,
                onRecargarTurnos = usuariosViewModel::recargarTurnos,
                onVerRegistrosTurno = { turnoId ->
                    navController.navigate(Ruta.VerRegistros.crear(turnoId))
                },
                onEditarTurno = { turnoId ->
                    navController.navigate(Ruta.ProgramarSesion.crear(turnoId))
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
            UsuariosScreen(
                usuarios = usuarios,
                usuarioActual = usuarioActual,
                filtro = filtro,
                onBuscar = usuariosViewModel::buscar,
                onEditar = { id -> navController.navigate(Ruta.DetalleUsuario.crear(id)) },
                onEliminarUsuario = usuariosViewModel::eliminar,
                onVolver = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuariosViewModel = usuariosViewModel,
                onModificarPerfil = {
                    navController.navigate(Ruta.EditarPerfil.ruta)
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
            DetalleUsuarioScreen(
                usuarioId = usuarioId,
                onVolver = { navController.navigateUp() }
            )
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