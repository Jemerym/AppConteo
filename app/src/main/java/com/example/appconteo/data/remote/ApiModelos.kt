package com.example.appconteo.data.remote

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
    val avatarUrl: String?
)

// Convierte el DTO de la API en el modelo que usa la app:
// las pantallas siguen trabajando con Usuario y no saben que existe una API.
fun UsuarioResponse.aUsuario() = Usuario(
    nombre = nombre,
    correo = correo,
    id = id,
    rolId = rolId,
    rolNombre = rolNombre,
    avatarUrl = avatarUrl
)
