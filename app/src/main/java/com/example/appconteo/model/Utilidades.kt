package com.example.appconteo.model

import java.security.MessageDigest

object Utilidades {
    private val PATRON_CORREO = Regex("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")

    fun esCorreoValido(correo: String): Boolean {
        return correo.isNotBlank() && PATRON_CORREO.matches(correo.trim())
    }

    fun hashContrasena(contrasena: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(contrasena.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
