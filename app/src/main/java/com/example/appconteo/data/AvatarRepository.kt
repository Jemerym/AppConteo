package com.example.appconteo.data

import com.example.appconteo.data.remote.AvatarApi
import com.example.appconteo.data.remote.RetrofitClient
import com.example.appconteo.data.remote.aUsuario
import com.example.appconteo.model.Usuario
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AvatarRepository(
    private val avatarApi: AvatarApi = RetrofitClient.avatarApi
) {

    suspend fun subir(id: Long, bytes: ByteArray): Usuario {
        val cuerpo = bytes.toRequestBody("image/jpeg".toMediaType())
        val parte = MultipartBody.Part.createFormData("archivo", "avatar.jpg", cuerpo)
        return llamarApi { avatarApi.subir(id, parte) }.aUsuario()
    }
}
