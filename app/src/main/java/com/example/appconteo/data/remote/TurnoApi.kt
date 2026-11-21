package com.example.appconteo.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Describe los endpoints de turnos-service (puerto 8083).
interface TurnoApi {

    // usuarioId = null -> todos los turnos (admin); con id -> solo los de ese observador
    @GET("api/turnos")
    suspend fun listar(@Query("usuarioId") usuarioId: Long?): List<TurnoResponse>

    @GET("api/turnos/{id}")
    suspend fun obtener(@Path("id") id: Long): TurnoResponse

    // Turno activo ahora según la hora del SERVIDOR. Responde 204 (sin cuerpo) si no tiene turno activo;
    // por eso se devuelve Response<...>: así el 204 llega como body() == null y no como error.
    @GET("api/turnos/actual")
    suspend fun actual(@Query("usuarioId") usuarioId: Long): Response<TurnoResponse>

    @POST("api/turnos")
    suspend fun crear(@Body datos: TurnoRequest): TurnoResponse

    @PUT("api/turnos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: TurnoRequest): TurnoResponse

    // CAMBIO: ya no se elimina; se anula y se puede restaurar
    @PUT("api/turnos/{id}/anular")
    suspend fun anular(@Path("id") id: Long): TurnoResponse

    @PUT("api/turnos/{id}/restaurar")
    suspend fun restaurar(@Path("id") id: Long): TurnoResponse

    @PUT("api/turnos/usuario/{usuarioId}/anular")
    suspend fun anularDeUsuario(@Path("usuarioId") usuarioId: Long)
}
