package com.example.appconteo.data

import com.example.appconteo.data.remote.ActualizarUsuarioRequest
import com.example.appconteo.data.remote.AvatarApi
import com.example.appconteo.data.remote.CambiarContrasenaRequest
import com.example.appconteo.data.remote.LoginRequest
import com.example.appconteo.data.remote.RecuperarClaveRequest
import com.example.appconteo.data.remote.RegistroRequest
import com.example.appconteo.data.remote.RetrofitClient
import com.example.appconteo.data.remote.UsuarioApi
import com.example.appconteo.data.remote.aUsuario
import com.example.appconteo.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

// Patrón Repository: el ÚNICO lugar que sabe DE DÓNDE vienen los datos.
//
//   Pantalla -> ViewModel -> UsuarioRepository -> Retrofit -> usuarios-service -> MySQL
//
// Antes el ViewModel llamaba a UsuarioDbHelper (SQLite). Ahora llama a esta clase.
// Cada función es suspend porque espera la respuesta de la red.
// Si algo falla, lanza Exception con un mensaje listo para mostrar al usuario.

class UsuarioRepository(
    private val usuarioApi: UsuarioApi = RetrofitClient.usuarioApi,
    private val avatarApi: AvatarApi = RetrofitClient.avatarApi
) {

    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario =
        llamar { usuarioApi.iniciarSesion(LoginRequest(correo, contrasena)) }.aUsuario()

    suspend fun registrar(usuario: Usuario): Usuario {
        val datos = RegistroRequest(
            nombre = usuario.nombre,
            correo = usuario.correo,
            contrasena = usuario.contrasena,
            palabraSecreta = usuario.palabraSecreta,
            rolId = usuario.rolId
        )
        return llamar { usuarioApi.registrar(datos) }.aUsuario()
    }

    suspend fun obtener(id: Long): Usuario =
        llamar { usuarioApi.obtener(id) }.aUsuario()

    // ifBlank { null }: sin texto no se envía ?buscar= y la API devuelve todos
    suspend fun listar(buscar: String = ""): List<Usuario> =
        llamar { usuarioApi.listar(buscar.ifBlank { null }) }.map { it.aUsuario() }

    suspend fun actualizar(usuario: Usuario): Usuario {
        val datos = ActualizarUsuarioRequest(
            nombre = usuario.nombre,
            correo = usuario.correo,
            rolId = usuario.rolId
        )
        return llamar { usuarioApi.actualizar(usuario.id, datos) }.aUsuario()
    }

    suspend fun cambiarContrasena(id: Long, actual: String, nueva: String) =
        llamar { usuarioApi.cambiarContrasena(id, CambiarContrasenaRequest(actual, nueva)) }

    suspend fun recuperarClave(correo: String, palabraSecreta: String, nuevaClave: String) =
        llamar { usuarioApi.recuperarClave(RecuperarClaveRequest(correo, palabraSecreta, nuevaClave)) }

    suspend fun eliminar(id: Long) =
        llamar { usuarioApi.eliminar(id) }

    // bytes = la imagen ya comprimida (ConvertidorImagen). Devuelve el usuario con su nueva avatarUrl.
    suspend fun subirAvatar(id: Long, bytes: ByteArray): Usuario {
        val cuerpo = bytes.toRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", cuerpo)
        return llamar { avatarApi.subir(id, parte) }.aUsuario()
    }

    // Ejecuta UNA llamada y traduce los errores a un mensaje legible:
    //  - HttpException: el servidor respondió con error. Spring manda {"message": "..."}.
    //  - IOException: no hubo respuesta (servidor apagado, sin red, URL mal escrita).
    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw Exception(mensajeDelServidor(e))
    } catch (e: IOException) {
        throw Exception("No se pudo conectar con el servidor")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = try {
            JSONObject(cuerpo ?: "").optString("message")
        } catch (ex: Exception) {
            ""
        }
        return mensaje.ifBlank { "Error del servidor (${e.code()})" }
    }
}
