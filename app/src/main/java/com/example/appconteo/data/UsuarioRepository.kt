package com.example.appconteo.data

import com.example.appconteo.data.remote.ActualizarUsuarioRequest
import com.example.appconteo.data.remote.CambiarContrasenaRequest
import com.example.appconteo.data.remote.LoginRequest
import com.example.appconteo.data.remote.RecuperarClaveRequest
import com.example.appconteo.data.remote.RegistroRequest
import com.example.appconteo.data.remote.RetrofitClient
import com.example.appconteo.data.remote.UsuarioApi
import com.example.appconteo.data.remote.aUsuario
import com.example.appconteo.model.Usuario

// Patrón Repository: el ÚNICO lugar que sabe DE DÓNDE vienen los datos de usuarios.
//
//   Pantalla -> ViewModel -> UsuarioRepository -> Retrofit -> usuarios-service -> MySQL
//
// Cada función es suspend porque espera la respuesta de la red.
// Si algo falla, lanza Exception con un mensaje listo para mostrar al usuario.

class UsuarioRepository(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario =
        llamarApi { usuarioApi.iniciarSesion(LoginRequest(correo, contrasena)) }.aUsuario()

    suspend fun registrar(usuario: Usuario): Usuario {
        val datos = RegistroRequest(
            nombre = usuario.nombre,
            correo = usuario.correo,
            contrasena = usuario.contrasena,
            palabraSecreta = usuario.palabraSecreta,
            rolId = usuario.rolId
        )
        return llamarApi { usuarioApi.registrar(datos) }.aUsuario()
    }

    suspend fun obtener(id: Long): Usuario =
        llamarApi { usuarioApi.obtener(id) }.aUsuario()

    // ifBlank { null }: sin texto no se envía ?buscar= y la API devuelve todos
    suspend fun listar(buscar: String = ""): List<Usuario> =
        llamarApi { usuarioApi.listar(buscar.ifBlank { null }) }.map { it.aUsuario() }

    suspend fun actualizar(usuario: Usuario): Usuario {
        val datos = ActualizarUsuarioRequest(
            nombre = usuario.nombre,
            correo = usuario.correo,
            rolId = usuario.rolId
        )
        return llamarApi { usuarioApi.actualizar(usuario.id, datos) }.aUsuario()
    }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String) =
        llamarApi { usuarioApi.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun recuperarClave(correo: String, palabraSecreta: String, nuevaClave: String) =
        llamarApi { usuarioApi.recuperarClave(RecuperarClaveRequest(correo, palabraSecreta, nuevaClave)) }

    suspend fun eliminar(id: Long) =
        llamarApi { usuarioApi.eliminar(id) }
}
