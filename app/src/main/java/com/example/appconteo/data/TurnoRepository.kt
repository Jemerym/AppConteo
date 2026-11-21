package com.example.appconteo.data

import com.example.appconteo.data.remote.RetrofitClient
import com.example.appconteo.data.remote.TurnoApi
import com.example.appconteo.data.remote.TurnoRequest
import com.example.appconteo.data.remote.aTurno
import com.example.appconteo.model.Turno

// Único lugar que sabe de dónde vienen los turnos:
//   Pantalla -> ViewModel -> TurnoRepository -> Retrofit -> turnos-service -> MySQL
class TurnoRepository(
    private val api: TurnoApi = RetrofitClient.turnoApi
) {

    suspend fun listar(usuarioId: Long? = null): List<Turno> =
        llamarApi { api.listar(usuarioId) }.map { it.aTurno() }

    suspend fun obtener(id: Long): Turno =
        llamarApi { api.obtener(id) }.aTurno()

    // Turno activo ahora (hora del servidor). null si el observador no tiene turno en este momento.
    suspend fun obtenerActual(usuarioId: Long): Turno? {
        val respuesta = llamarApi { api.actual(usuarioId) }
        if (!respuesta.isSuccessful) throw Exception("Error del servidor (${respuesta.code()})")
        return respuesta.body()?.aTurno()
    }

    suspend fun crear(usuarioId: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Turno =
        llamarApi { api.crear(TurnoRequest(usuarioId, acceso, horaInicio, horaFin, fecha)) }.aTurno()

    suspend fun actualizar(
        id: Long, usuarioId: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String
    ): Turno =
        llamarApi { api.actualizar(id, TurnoRequest(usuarioId, acceso, horaInicio, horaFin, fecha)) }.aTurno()

    suspend fun anular(id: Long): Turno =
        llamarApi { api.anular(id) }.aTurno()

    suspend fun restaurar(id: Long): Turno =
        llamarApi { api.restaurar(id) }.aTurno()

    suspend fun anularDeUsuario(usuarioId: Long) =
        llamarApi { api.anularDeUsuario(usuarioId) }
}
