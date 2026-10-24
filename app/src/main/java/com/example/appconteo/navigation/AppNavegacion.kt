package com.example.appconteo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appconteo.ui.screens.UsuariosScreen
import com.example.appconteo.ui.screens.BienvenidoScreen
import com.example.appconteo.ui.screens.LoginScreen
import com.example.appconteo.ui.screens.PerfilScreen
import com.example.appconteo.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val usuarios by usuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val filtro by usuariosViewModel.filtro.collectAsStateWithLifecycle()
    val startDestination = if (usuarioActual != null) {
        val esAdmin = usuarioActual?.correo.equals("admin@demo.cl", ignoreCase = true) || usuarioActual?.rolId == 1L
        if (esAdmin) Ruta.Admin.ruta else Ruta.Perfil.ruta
    } else {
        Ruta.Bienvenido.ruta
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Ruta.Bienvenido.ruta) {
            BienvenidoScreen(
                onNavegarLogin = { navController.navigate(Ruta.Login.ruta) },
            )
        }
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    val usuario = usuariosViewModel.usuarioActual.value
                    val esAdmin = usuario?.correo.equals("admin@demo.cl", ignoreCase = true) || usuario?.rolId == 1L
                    val destino = if (esAdmin) Ruta.Admin.ruta else Ruta.Perfil.ruta
                    navController.navigate(destino) {
                        popUpTo(Ruta.Bienvenido.ruta) { inclusive = true }
                    }
                },
                usuariosViewModel = usuariosViewModel
            )
        }

        composable(Ruta.Usuarios.ruta) {
            UsuariosScreen(
                usuarios = usuarios,
                usuarioActual = usuarioActual,
                filtro = filtro,
                onBuscar = { texto -> usuariosViewModel.buscar(texto) },
                onEliminarUsuario = { usuario -> usuariosViewModel.eliminar(usuario) },
                onVolver = { navController.navigate(Ruta.Admin.ruta) }
            )
        }

        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuario = usuarioActual,
                onFotoTomada = { foto ->
                    usuariosViewModel.cambiarAvatarDesdeCamara(foto)
                },
                onImagenElegida = { uri ->
                    usuariosViewModel.cambiarAvatarDesdeGaleria(uri)
                },
                onCerrarSesion = {
                    navController.navigate(Ruta.Bienvenido.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                    usuariosViewModel.cerrarSesion()
                }
            )
        }
    }
}