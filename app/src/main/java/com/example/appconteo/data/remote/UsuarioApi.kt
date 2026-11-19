package com.example.appconteo.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Solo DESCRIBE los endpoints de usuarios-service; Retrofit escribe la llamada HTTP.
// suspend = espera la respuesta sin congelar la pantalla (se llama desde viewModelScope).
// Si el servidor responde 400/401/404/409..., Retrofit lanza HttpException.

interface UsuarioApi {

    @GET("api/usuarios")
    suspend fun listar(@Query("buscar") buscar: String?): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios")
    suspend fun registrar(@Body datos: RegistroRequest): UsuarioResponse

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body datos: LoginRequest): UsuarioResponse

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ActualizarUsuarioRequest): UsuarioResponse

    // Sin tipo de retorno (Unit): el servidor responde 204 No Content
    @PUT("api/usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Long, @Body datos: CambiarContrasenaRequest)

    @POST("api/usuarios/recuperar-clave")
    suspend fun recuperarClave(@Body datos: RecuperarClaveRequest)

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
