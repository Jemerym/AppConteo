package com.example.appconteo.data

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

// Ejecuta UNA llamada a la API y traduce los errores a un mensaje legible.
// Lo comparten todos los repositories (usuarios, avatar, turnos y los que vengan).
//  - HttpException: el servidor respondió con error. Spring manda {"message": "..."}.
//  - IOException: no hubo respuesta (servidor apagado, sin red, URL mal escrita).
internal suspend fun <T> llamarApi(peticion: suspend () -> T): T = try {
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
