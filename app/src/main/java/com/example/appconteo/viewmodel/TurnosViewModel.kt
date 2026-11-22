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

    // ESPEJO LOCAL TEMPORAL: el historial (aún en SQLite) tiene clave foránea a los turnos.
    // Se elimina cuando exista registros-service.
    private val db = UsuarioDbHelper(application)

    // Turnos para MOSTRAR en "Mis turnos". Se vuelven a pedir al servidor al entrar a la pantalla
    // y periódicamente; nunca se usan para decidir cuál es el turno actual.
    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()

    // Turno activo AHORA según el SERVIDOR (con la hora del servidor, no la del teléfono).
    private val _turnoActual = MutableStateFlow<Turno?>(null)
    val turnoActual: StateFlow<Turno?> = _turnoActual.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var usuario: Usuario? = null
    private val esAdmin: Boolean get() = usuario?.esAdmin == true

    /** La navegación lo llama cuando cambia el usuario (login, sesión restaurada o cierre de sesión). */
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

    // ---------- carga desde el servidor ----------

    /** Carga la lista y avisa con un Toast si falla (úsalo para acciones del usuario). */
    fun recargar() = cargarLista(silencioso = false)

    /** Igual, pero sin Toast si falla: para el refresco automático cada pocos segundos. */
    fun refrescarEnSegundoPlano() = cargarLista(silencioso = true)

    // El admin descarga todos los turnos; un observador solo los suyos.
    private fun cargarLista(silencioso: Boolean) {
        val u = usuario ?: return
        ejecutar(onError = { if (!silencioso) _mensaje.value = "❌ $it" }) {
            val lista = repo.listar(if (u.esAdmin) null else u.id)
            db.sincronizarTurnos(lista)
            if (usuario?.id == u.id) _turnos.value = lista
        }
    }

    /**
     * Pregunta al servidor cuál es el turno activo ahora (GET /api/turnos/actual).
     * Usa la hora del servidor, así que no depende de la hora del dispositivo.
     * Si no hay conexión conserva el último valor conocido.
     */
    fun actualizarTurnoActual() {
        val u = usuario ?: return
        if (u.esAdmin) return   // el admin no cuenta personas
        ejecutar(onError = { }) {
            val turno = repo.obtenerActual(u.id)
            if (turno != null) db.sincronizarTurnos(listOf(turno))   // el historial local lo referencia
            if (usuario?.id == u.id) _turnoActual.value = turno
        }
    }

    // Lo lee InicioUsuariosScreen de forma sincrónica (al mostrarse y al registrar a una persona),
    // pero el dato lo decidió el servidor.
    fun obtenerTurnoActual(idUsuario: Long): Turno? =
        _turnoActual.value?.takeIf { it.usuarioId == idUsuario }

    /** Trae UN turno directo del servidor (para editarlo con datos al día, no los de una lista vieja). */
    fun obtener(id: Long, onResultado: (Turno) -> Unit) {
        ejecutar {
            onResultado(repo.obtener(id))
        }
    }

    // ---------- modificaciones (las valida el servidor) ----------

    /** Crea el turno (turnoId = 0) o lo modifica (turnoId > 0). El servidor valida los conflictos. */
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

    // CAMBIO: antes se eliminaba. Ahora se ANULA (se conserva, deja de ocupar el tramo) y se puede restaurar.
    // El espejo local NO se toca: los registros hechos en ese turno lo siguen referenciando.
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
            val restaurado = repo.restaurar(idTurno)   // 409 si el tramo ya lo ocupa otro turno
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
