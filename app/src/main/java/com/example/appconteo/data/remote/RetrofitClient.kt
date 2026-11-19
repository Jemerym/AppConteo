package com.example.appconteo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Crea UNA sola vez el cliente de cada microservicio.
// 10.0.2.2 = "localhost" del PC visto desde el EMULADOR.
// En un celular físico usa la IP del PC en la misma WiFi (ej: http://192.168.1.10:8082/).
// La URL base DEBE terminar en "/".

object RetrofitClient {

    private const val URL_USUARIOS = "http://192.168.1.5:8082/"
    // Cuando existan los otros servicios se agregan aquí:
    // private const val URL_TURNOS = "http://10.0.2.2:8083/"
    // private const val URL_REGISTROS = "http://10.0.2.2:8084/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // by lazy = se crea la primera vez que se usa
    val usuarioApi: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }

    // Misma URL que usuarios, otra interfaz (la subida de la foto)
    val avatarApi: AvatarApi by lazy { crear(URL_USUARIOS).create(AvatarApi::class.java) }
}
