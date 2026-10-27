package com.example.appconteo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appconteo.ui.screens.AdminScreen
import com.example.appconteo.ui.screens.DetalleUsuarioScreen
import com.example.appconteo.ui.screens.EditarPerfilScreen
import com.example.appconteo.ui.screens.InicioUsuariosScreen
import com.example.appconteo.ui.screens.LoginScreen
import com.example.appconteo.ui.screens.PerfilScreen
import com.example.appconteo.ui.screens.RegistroScreen
import com.example.appconteo.ui.screens.UsuariosScreen
import com.example.appconteo.viewmodel.ConteoViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val usuariosViewModel: UsuariosViewModel = viewModel()
    val conteoViewModel: ConteoViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val historial by usuariosViewModel.historial.collectAsStateWithLifecycle()

    val startDestination = if (usuarioActual != null) {
        val esAdmin = usuarioActual?.correo.equals(
            "admin@demo.cl",
            ignoreCase = true
        ) || usuarioActual?.rolId == 1L
        if (esAdmin) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta
    } else {
        Ruta.Login.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onRegistroExitoso = { usuario ->
                    usuariosViewModel.registrar(usuario)
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Registro.ruta) { inclusive = true }
                    }
                },
                onIrLogin = { navController.navigate(Ruta.Login.ruta) },
            )
        }
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    val usuario = usuariosViewModel.usuarioActual.value
                    val esAdmin = usuario?.correo.equals(
                        "admin@demo.cl",
                        ignoreCase = true
                    ) || usuario?.rolId == 1L
                    val destino = if (esAdmin) Ruta.Admin.ruta else Ruta.InicioUsuarios.ruta

                    navController.navigate(destino) {
                    }
                },
                usuariosViewModel = usuariosViewModel,
                onIrRegistro = {navController.navigate(Ruta.Registro.ruta){
                    popUpTo(Ruta.Login.ruta) { inclusive = true }
                } }
            )
        }

        composable(Ruta.Admin.ruta) {
            AdminScreen(
                historial = historial,
                usuarioActual = usuarioActual,
                conteoViewModel = conteoViewModel,
                onVerUsuarios = { navController.navigate(Ruta.Usuarios.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                    usuariosViewModel.cerrarSesion()
                }
            )
        }

        composable(Ruta.InicioUsuarios.ruta) {
            InicioUsuariosScreen(
                usuario = usuarioActual,
                usuariosViewModel = usuariosViewModel,
                conteoViewModel = conteoViewModel,
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCerrarSesion = {
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                    usuariosViewModel.cerrarSesion()
                }
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
                    onGuardar = { editado -> usuariosViewModel.actualizar(editado)
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}