package com.example.appconteo.model

object Validaciones {

    private val regexNombre = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")
    private val regexCorreo = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    data class Criterio(
        val texto: String,   // lo que se muestra en los indicadores
        val error: String,   // mensaje cuando no se cumple
        val cumple: (String) -> Boolean
    )

    val criteriosContrasena = listOf(
        Criterio("Mínimo 8 caracteres", "Mínimo 8 caracteres") { it.length >= 8 },
        Criterio("Al menos 1 mayúscula (A-Z)", "Debe tener una mayúscula") { c -> c.any { it.isUpperCase() } },
        Criterio("Al menos 1 minúscula (a-z)", "Debe tener una minúscula") { c -> c.any { it.isLowerCase() } },
        Criterio("Al menos 1 número (0-9)", "Debe tener un número") { c -> c.any { it.isDigit() } },
        Criterio("Al menos 1 símbolo (@#$%)", "Debe tener un carácter especial (ej: ! # $ %)") { c -> c.any { !it.isLetterOrDigit() } }
    )

    fun contrasena(valor: String): String? =
        criteriosContrasena.firstOrNull { !it.cumple(valor) }?.error

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        !valor.matches(regexNombre) -> "El nombre solo debe contener letras"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !valor.matches(regexCorreo) -> "Ingresa un correo válido (ej: usuario@duocuc.cl)"
        else -> null
    }

}
