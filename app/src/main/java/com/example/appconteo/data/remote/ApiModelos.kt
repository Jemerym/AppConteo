package com.example.appconteo.data.remote

import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario

// DTO = clases con EXACTAMENTE la forma del JSON de usuarios-service.
//  *Request  -> lo que la app ENVÍA
//  *Response -> lo que la app RECIBE
// Gson une cada propiedad con la clave JSON del mismo nombre.

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val palabraSecreta: String,
    val rolId: Long
)

data class ActualizarUsuarioRequest(
    val nombre: String,
    val correo: String,
    val rolId: Long
)

data class CambiarContrasenaRequest(
    val actual: String,
    val nueva: String
)

data class RecuperarClaveRequest(
    val correo: String,
    val palabraSecreta: String,
    val nuevaClave: String
)

// La API NUNCA devuelve contraseña ni palabra secreta.
// avatarUrl llega null si el usuario no ha subido foto.
data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rolId: Long,
    val rolNombre: String,
    val avatarUrl: String?,
    val activo: Boolean?          // null si el servidor es antiguo -> se toma como activo
)

// Convierte el DTO de la API en el modelo que usa la app:
// las pantallas siguen trabajando con Usuario y no saben que existe una API.
fun UsuarioResponse.aUsuario() = Usuario(
    nombre = nombre,
    correo = correo,
    id = id,
    rolId = rolId,
    rolNombre = rolNombre,
    avatarUrl = avatarUrl,
    activo = activo != false
)

// ---------------- Turnos (turnos-service) ----------------

data class TurnoRequest(
    val usuarioId: Long,
    val acceso: Int,
    val horaInicio: String,   // "10:00"
    val horaFin: String,      // "10:30"
    val fecha: String         // "2026-11-19"
)

data class TurnoResponse(
    val id: Long,
    val usuarioId: Long,
    val observadorNombre: String,
    val acceso: Int,
    val horaInicio: String,
    val horaFin: String,
    val fecha: String,
    val anulado: Boolean?
)

fun TurnoResponse.aTurno() = Turno(
    id = id,
    usuarioId = usuarioId,
    observadorNombre = observadorNombre,
    acceso = acceso,
    horaInicio = horaInicio,
    horaFin = horaFin,
    fecha = fecha,
    anulado = anulado == true
)
