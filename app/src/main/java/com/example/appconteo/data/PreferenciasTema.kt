package com.example.appconteo.data

import android.content.Context
import androidx.core.content.edit
import com.example.appconteo.ui.theme.TemaApp

// Guarda en el teléfono el tema elegido (mismo estilo que SesionPreferencias).
class PreferenciasTema(context: Context) {

    private val prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_TEMA = "tema"
    }

    fun leer(): TemaApp {
        val guardado = prefs.getString(CLAVE_TEMA, null)
        return TemaApp.entries.firstOrNull { it.name == guardado } ?: TemaApp.SISTEMA
    }

    fun guardar(tema: TemaApp) {
        prefs.edit { putString(CLAVE_TEMA, tema.name) }
    }
}
