package com.example.appconteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.navigation.AppNavegacion
import com.example.appconteo.viewmodel.ConteoViewModel
import com.example.appconteo.viewmodel.TemaViewModel
import com.example.appconteo.viewmodel.UsuariosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // El tema elegido en Perfil se aplica a TODA la app (y queda guardado al cerrarla)
            val temaViewModel: TemaViewModel = viewModel()
            val tema by temaViewModel.tema.collectAsStateWithLifecycle()

            AppConteoTheme(tema = tema) {
                val usuariosViewModel: UsuariosViewModel = viewModel()
                val conteoViewModel: ConteoViewModel = viewModel()
                AppNavegacion(
                    usuariosViewModel = usuariosViewModel,
                    conteoViewModel = conteoViewModel,
                    temaActual = tema,
                    onCambiarTema = temaViewModel::cambiarTema
                )
            }
        }
    }
}
