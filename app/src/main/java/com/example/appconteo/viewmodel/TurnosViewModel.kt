package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconteo.data.TurnoRepository
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TurnosViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TurnoRepository()
    private val db = UsuarioDbHelper(application)

    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()

    private val _turnoActual = MutableStateFlow<Turno?>(null)
    val turnoActual: StateFlow<Turno?> = _turnoActual.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var usuario: Usuario? = null
    private val esAdmin: Boolean get() = usuario?.esAdmin == true

    fun establecerUsuario(nuevo: Usuario?) {
        val cambioDeUsuario = usuario?.id != nuevo?.id
        usuario = nuevo
        when {
            nuevo == null -> {
                _turnos.value = emptyList()
                _turnoActual.value = null
            }
            cambioDeUsuario -> {
                _turnoActual.value = null
                recargar()
                actualizarTurnoActual()
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun recargar() = cargarLista(silencioso = false)

    fun refrescarEnSegundoPlano() = cargarLista(silencioso = true)

    private fun cargarLista(silencioso: Boolean) {
        val u = usuario ?: return
        ejecutar(onError = { if (!silencioso) _mensaje.value = "❌ $it" }) {
            val lista = repo.listar(if (u.esAdmin) null else u.id)
            db.sincronizarTurnos(lista)
            if (usuario?.id == u.id) _turnos.value = lista
        }
    }

    fun actualizarTurnoActual() {
        val u = usuario ?: return
        if (u.esAdmin) return
        ejecutar(onError = { }) {
            val turno = repo.obtenerActual(u.id)
            if (turno != null) db.sincronizarTurnos(listOf(turno))
            if (usuario?.id == u.id) _turnoActual.value = turno
        }
    }

    fun obtenerTurnoActual(idUsuario: Long): Turno? =
        _turnoActual.value?.takeIf { it.usuarioId == idUsuario }

    fun obtener(id: Long, onResultado: (Turno) -> Unit) {
        ejecutar {
            onResultado(repo.obtener(id))
        }
    }

    fun guardar(
        turnoId: Long,
        usuarioId: Long,
        acceso: Int,
        horaInicio: String,
        horaFin: String,
        fecha: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!esAdmin) {
            onError("Solo un administrador puede programar turnos")
            return
        }
        ejecutar(onError) {
            val edicion = turnoId > 0L
            if (edicion) repo.actualizar(turnoId, usuarioId, acceso, horaInicio, horaFin, fecha)
            else repo.crear(usuarioId, acceso, horaInicio, horaFin, fecha)

            _mensaje.value = if (edicion) "✅ Turno actualizado con éxito" else "✅ Turno programado con éxito"
            recargar()
            onExito()
        }
    }

    fun anular(idTurno: Long) {
        if (!esAdmin) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            val anulado = repo.anular(idTurno)
            reemplazarEnLista(anulado)
            _mensaje.value = "✅ Turno anulado"
        }
    }

    fun restaurar(idTurno: Long) {
        if (!esAdmin) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            val restaurado = repo.restaurar(idTurno)
            reemplazarEnLista(restaurado)
            _mensaje.value = "✅ Turno restaurado"
        }
    }

    private fun reemplazarEnLista(turno: Turno) {
        _turnos.value = _turnos.value.map { if (it.id == turno.id) turno else it }
    }

    private fun ejecutar(
        onError: (String) -> Unit = { _mensaje.value = "❌ $it" },
        bloque: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                bloque()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e.message ?: "Error inesperado")
            }
        }
    }

    override fun onCleared() {
        db.close()
    }
}
