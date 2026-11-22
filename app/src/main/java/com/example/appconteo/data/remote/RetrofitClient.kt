package com.example.appconteo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Crea UNA sola vez el cliente de cada microservicio.
// CAMBIO: ahora todo pasa por el API GATEWAY (puerto 8080), que reparte cada ruta al microservicio que
// corresponde (/api/usuarios -> usuarios-ms, /api/turnos -> turnos-ms, /api/registros -> registros-ms).
// 10.0.2.2 = "localhost" del PC visto desde el EMULADOR.
// En un celular físico usa la IP del PC en la misma WiFi (ej: http://192.168.1.10:8080/).
// La URL base DEBE terminar en "/".

object RetrofitClient {

    private const val URL_GATEWAY = "http://192.168.1.5:8080/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // Un solo Retrofit para todo: la misma URL (el gateway) sirve para todas las interfaces
    private val retrofit: Retrofit by lazy { crear(URL_GATEWAY) }

    // by lazy = se crea la primera vez que se usa
    val usuarioApi: UsuarioApi by lazy { retrofit.create(UsuarioApi::class.java) }

    // La subida de la foto (usuarios-ms)
    val avatarApi: AvatarApi by lazy { retrofit.create(AvatarApi::class.java) }

    val turnoApi: TurnoApi by lazy { retrofit.create(TurnoApi::class.java) }

    val registroApi: RegistroApi by lazy { retrofit.create(RegistroApi::class.java) }
}
