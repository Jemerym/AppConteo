package com.example.appconteo.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val URL_USUARIOS = "http://192.168.1.5:8082/"
    private const val URL_TURNOS = "http://192.168.1.5:8083/"
    private const val URL_REGISTROS = "http://192.168.1.5:8084/"

    private fun crear(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarioApi: UsuarioApi by lazy { crear(URL_USUARIOS).create(UsuarioApi::class.java) }
    val avatarApi: AvatarApi by lazy { crear(URL_USUARIOS).create(AvatarApi::class.java) }
    val turnoApi: TurnoApi by lazy { crear(URL_TURNOS).create(TurnoApi::class.java) }
    val registroApi: RegistroApi by lazy { crear(URL_REGISTROS).create(RegistroApi::class.java) }
}
