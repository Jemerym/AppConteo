package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appconteo.data.PreferenciasTema
import com.example.appconteo.ui.theme.TemaApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Lo crea MainActivity (vive mientras vive la app) y se lo pasa a la navegación,
// así PerfilScreen cambia el tema y MainActivity lo aplica a todas las pantallas al instante.
class TemaViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencias = PreferenciasTema(application)

    private val _tema = MutableStateFlow(preferencias.leer())
    val tema: StateFlow<TemaApp> = _tema.asStateFlow()

    fun cambiarTema(nuevo: TemaApp) {
        _tema.value = nuevo
        preferencias.guardar(nuevo)
    }
}
