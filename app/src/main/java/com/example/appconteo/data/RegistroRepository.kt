package com.example.appconteo.data

import com.example.appconteo.data.remote.ModificarRegistroAforoRequest
import com.example.appconteo.data.remote.RegistroAforoRequest
import com.example.appconteo.data.remote.RegistroApi
import com.example.appconteo.data.remote.RetrofitClient
import com.example.appconteo.data.remote.aHistorial
import com.example.appconteo.model.HistorialRegistros

class RegistroRepository(
    private val api: RegistroApi = RetrofitClient.registroApi
) {

    suspend fun listar(
        usuarioId: Long? = null,
        fecha: String? = null,
        incluirAnulados: Boolean = false
    ): List<HistorialRegistros> =
        llamarApi { api.listar(usuarioId = usuarioId, fecha = fecha, incluirAnulados = incluirAnulados) }
            .map { it.aHistorial() }

    suspend fun crear(datos: RegistroAforoRequest): HistorialRegistros =
        llamarApi { api.crear(datos) }.aHistorial()

    suspend fun modificar(id: Long, acceso: Int, tipoGrupo: String, rangoEtario: String): HistorialRegistros =
        llamarApi { api.modificar(id, ModificarRegistroAforoRequest(acceso, tipoGrupo, rangoEtario)) }.aHistorial()

    suspend fun anular(id: Long): HistorialRegistros =
        llamarApi { api.anular(id) }.aHistorial()

    suspend fun restaurar(id: Long): HistorialRegistros =
        llamarApi { api.restaurar(id) }.aHistorial()
}
