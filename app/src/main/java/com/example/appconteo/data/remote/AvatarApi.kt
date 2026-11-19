package com.example.appconteo.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Subida de la foto de perfil. El campo se llama "archivo" (igual que @RequestParam del controller).
interface AvatarApi {

    @Multipart
    @POST("api/usuarios/{id}/avatar")
    suspend fun subir(@Path("id") id: Long, @Part archivo: MultipartBody.Part): UsuarioResponse
}
