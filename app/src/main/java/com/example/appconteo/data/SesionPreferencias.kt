package com.example.appconteo.data

import android.content.Context
import androidx.core.content.edit
import com.example.appconteo.model.Usuario

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val CLAVE_NOMBRE = "nombre"
        private const val CLAVE_CORREO = "correo"
        private const val CLAVE_ROL_ID = "rol_id"
        private const val CLAVE_ROL_NOMBRE = "rol_nombre"
        private const val SIN_SESION = -1L
    }

    // CAMBIO: antes guardaba solo el id y el resto se buscaba en SQLite.
    // Ahora guarda lo necesario para saber, al abrir la app, quién es y si es admin.
    fun guardar(usuario: Usuario) {
        prefs.edit {
            putLong(CLAVE_ID_USUARIO, usuario.id)
            putString(CLAVE_NOMBRE, usuario.nombre)
            putString(CLAVE_CORREO, usuario.correo)
            putLong(CLAVE_ROL_ID, usuario.rolId)
            putString(CLAVE_ROL_NOMBRE, usuario.rolNombre)
        }
    }

    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    // El usuario de la sesión guardada, o null si no hay sesión
    fun leerUsuario(): Usuario? {
        val id = leer()
        if (id == SIN_SESION) return null
        return Usuario(
            id = id,
            nombre = prefs.getString(CLAVE_NOMBRE, "") ?: "",
            correo = prefs.getString(CLAVE_CORREO, "") ?: "",
            rolId = prefs.getLong(CLAVE_ROL_ID, 0L),
            rolNombre = prefs.getString(CLAVE_ROL_NOMBRE, "") ?: ""
        )
    }

    fun borrar() {
        prefs.edit { clear() }
    }
}