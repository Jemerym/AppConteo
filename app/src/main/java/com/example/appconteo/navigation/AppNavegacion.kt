package com.example.appconteo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appconteo.ui.screens.BienvenidoScreen
import com.example.appconteo.ui.screens.LoginScreen
import com.example.appconteo.ui.screens.PerfilScreen
import com.example.appconteo.ui.screens.RegistroScreen
import com.example.appconteo.viewmodel.UsuariosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val usuariosViewModel: UsuariosViewModel = viewModel()
    val usuarioActual by usuariosViewModel.usuarioActual.collectAsStateWithLifecycle()

    val startDestination = if (usuarioActual != null) Ruta.Perfil.ruta else Ruta.Bienvenido.ruta

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Ruta.Bienvenido.ruta) {
            BienvenidoScreen(
                onNavegarLogin = { navController.navigate(Ruta.Login.ruta) },
                onNavegarRegistro = { navController.navigate(Ruta.Registro.ruta) }
            )
        }
        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Ruta.Perfil.ruta) {
                        popUpTo(Ruta.Bienvenido.ruta) { inclusive = true }
                    }
                },
                onIrRegistro = { navController.navigate(Ruta.Registro.ruta) },
                usuariosViewModel = usuariosViewModel
            )
        }
        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                onRegistroExitoso = { usuario ->
                    usuariosViewModel.registrar(usuario)
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.Registro.ruta) { inclusive = true }
                    }
                },
                onIrLogin = { navController.navigate(Ruta.Login.ruta) }
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
                    usuariosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Bienvenido.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}