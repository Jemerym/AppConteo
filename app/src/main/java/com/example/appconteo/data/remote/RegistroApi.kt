package com.example.appconteo.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Describe los endpoints de registros-service (puerto 8084).
interface RegistroApi {

    // Todos los filtros son opcionales: si van null no se envían.
    @GET("api/registros")
    suspend fun listar(
        @Query("usuarioId") usuarioId: Long? = null,
        @Query("turnoId") turnoId: Long? = null,
        @Query("fecha") fecha: String? = null,
        @Query("incluirAnulados") incluirAnulados: Boolean = false
        ): List<RegistroAforoResponse>

    // La fecha y la hora del registro las pone el servidor.
    @POST("api/registros")
    suspend fun crear(@Body datos: RegistroAforoRequest): RegistroAforoResponse

    @PUT("api/registros/{id}")
    suspend fun modificar(@Path("id") id: Long, @Body datos: ModificarRegistroAforoRequest): RegistroAforoResponse

    @PUT("api/registros/{id}/anular")
    suspend fun anular(@Path("id") id: Long): RegistroAforoResponse

    @PUT("api/registros/{id}/restaurar")
    suspend fun restaurar(@Path("id") id: Long): RegistroAforoResponse
}
