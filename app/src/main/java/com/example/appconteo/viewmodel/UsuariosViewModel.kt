package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconteo.data.SesionPreferencias
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.data.UsuarioRepository
import com.example.appconteo.model.CambiosPerfil
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    // USUARIOS -> ahora vienen del microservicio usuarios-service (UsuarioRepository).
    private val repo = UsuarioRepository()

    // SQLite se mantiene SOLO para turnos e historial (todavía no tienen microservicio)
    // y como "espejo" local de los usuarios (ver espejar()).
    private val db = UsuarioDbHelper(application)
    private val sesion = SesionPreferencias(application)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val roles: List<Rol> = db.listarRoles()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _historial = MutableStateFlow<List<HistorialRegistros>>(emptyList())
    val historial: StateFlow<List<HistorialRegistros>> = _historial.asStateFlow()

    private val _turnos = MutableStateFlow<List<Turno>>(emptyList())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()

    val esAdministrador: Boolean
        get() = _usuarioActual.value?.rolId == Rol.ID_ADMINISTRADOR

    // Evita enviar dos veces la misma acción si el usuario toca el botón repetido.
    private var accionEnCurso = false

    init {
        // El usuario de la sesión se lee del espejo local para decidir la pantalla inicial al instante;
        // luego se refresca desde el servidor.
        val idSesion = sesion.leer()
        _usuarioActual.value = if (idSesion != -1L) db.buscarPorId(idSesion) else null
        recargar()
        if (idSesion != -1L) recargarUsuarioActual()
    }

    // ---------- helpers de red ----------

    /** Lanza una llamada de red en segundo plano. Si falla, avisa con onError (por defecto, un Toast). */
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

    /** Igual que ejecutar, pero ignora un segundo toque mientras la acción anterior sigue en curso. */
    private fun accion(onError: (String) -> Unit, bloque: suspend () -> Unit) {
        if (accionEnCurso) return
        accionEnCurso = true
        ejecutar(onError) {
            try {
                bloque()
            } finally {
                accionEnCurso = false
            }
        }
    }

    // Copia los usuarios del servidor a SQLite para que turnos e historial (que aún son locales)
    // puedan seguir referenciándolos. Se elimina cuando existan turnos-service y registros-service.
    private fun espejar(lista: List<Usuario>) {
        db.sincronizarUsuarios(lista)
    }

    // ---------- carga de datos ----------

    fun recargar() {
        _historial.value = db.listarHistorial()
        _turnos.value = db.listarTurnos()
        cargarUsuarios()
    }

    // Solo el administrador necesita la lista completa de usuarios.
    private fun cargarUsuarios() {
        if (!esAdministrador) return
        ejecutar {
            val lista = repo.listar(_filtro.value.trim())
            espejar(lista)
            _usuarios.value = lista
        }
    }

    fun recargarUsuarioActual() {
        val idSesion = sesion.leer()
        if (idSesion == -1L) {
            _usuarioActual.value = null
            return
        }
        // En silencio: si no hay red se conserva el usuario que ya está en pantalla.
        ejecutar(onError = { if (it == "Usuario no encontrado") cerrarSesion() }) {
            val u = repo.obtener(idSesion)
            espejar(listOf(u))
            _usuarioActual.value = u
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        cargarUsuarios()
    }

    // Usado por ProgramarScreen (modo edición). Busca primero en la lista ya descargada.
    fun buscarPorId(id: Long): Usuario? = _usuarios.value.find { it.id == id } ?: db.buscarPorId(id)

    // ---------- sesión y cuenta ----------

    fun iniciarSesion(correo: String, contrasena: String, onExito: () -> Unit, onError: (String) -> Unit) {
        accion(onError) {
            val usuario = repo.iniciarSesion(correo, contrasena)
            espejar(listOf(usuario))
            _usuarioActual.value = usuario
            sesion.guardar(usuario.id)
            recargar()
            onExito()
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        _usuarios.value = emptyList()
        sesion.borrar()
    }

    fun registrar(usuario: Usuario, onExito: () -> Unit, onError: (String) -> Unit) {
        accion(onError) {
            repo.registrar(usuario)
            _mensaje.value = "✅ Usuario registrado con éxito"
            onExito()
        }
    }

    fun restablecerClave(
        correo: String,
        palabraSecreta: String,
        nuevaClave: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        accion(onError) {
            repo.recuperarClave(correo.trim(), palabraSecreta.trim(), nuevaClave)
            _mensaje.value = "✅ Clave actualizada con éxito"
            onExito()
        }
    }

    fun actualizarPerfil(
        original: Usuario,
        cambios: CambiosPerfil,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        accion(onError) {
            // 1) El servidor confirma que la contraseña actual es correcta.
            try {
                repo.iniciarSesion(original.correo, cambios.contrasenaActual)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val sinRed = e.message?.startsWith("No se pudo conectar") == true
                throw if (sinRed) e else Exception("Contraseña actual incorrecta")
            }
            // 2) Nombre.
            val actualizado = repo.actualizar(original.copy(nombre = cambios.nombre))
            // 3) Contraseña nueva (opcional).
            cambios.nuevaContrasena?.takeIf { it.isNotBlank() }?.let { nueva ->
                repo.cambiarContrasena(original.id, cambios.contrasenaActual, nueva)
            }
            espejar(listOf(actualizado))
            _usuarioActual.value = actualizado
            _mensaje.value = "✅ Usuario '${actualizado.nombre}' actualizado"
            onExito()
        }
    }

    fun actualizarAvatar(id: Long, avatar: ByteArray) {
        ejecutar {
            _usuarioActual.value = repo.subirAvatar(id, avatar)
            _mensaje.value = "✅ Foto de perfil actualizada"
        }
    }

    fun eliminar(usuario: Usuario) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            repo.eliminar(usuario.id)
            db.eliminarUsuario(usuario.id)   // espejo local (borra también sus turnos, como antes)
            _mensaje.value = "✅ Usuario '${usuario.nombre}' eliminado"
            if (_usuarioActual.value?.id == usuario.id) cerrarSesion() else recargar()
        }
    }

    // ---------- HISTORIAL (sigue en SQLite hasta crear registros-service) ----------

    fun registrarEnHistorial(
        observadorNombre: String,
        tipo: String,
        acceso: Int = 1,
        idTurno: Long = 0,
        usuarioId: Long = 0,
        tipoGrupo: String = "Solo",
        rangoEtario: String = "25-34",
        codigoBoleta: String? = null
    ): Boolean {
        val horaActual = Fechas.horaActual()
        val resultado = db.insertarHistorial(
            observador = observadorNombre,
            tipo = tipo,
            acceso = acceso,
            idTurno = idTurno,
            usuarioId = usuarioId,
            tipoGrupo = tipoGrupo,
            rangoEtario = rangoEtario,
            hora = horaActual,
            codigoBoleta = codigoBoleta
        )
        if (resultado == -1L) return false
        _historial.value = db.listarHistorial()
        return true
    }

    fun actualizarHistorial(idHistorial: Long, acceso: Int, tipoGrupo: String, rangoEtario: String) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        db.actualizarHistorial(idHistorial, acceso, tipoGrupo, rangoEtario)
        _historial.value = db.listarHistorial()
        _mensaje.value = "✅ Registro actualizado"
    }

    fun eliminarHistorial(idHistorial: Long) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        db.eliminarHistorial(idHistorial)
        _historial.value = db.listarHistorial()
        _mensaje.value = "✅ Registro eliminado"
    }

    // ---------- TURNOS (siguen en SQLite hasta crear turnos-service) ----------

    fun recargarTurnos() {
        _turnos.value = db.listarTurnos()
    }

    fun eliminarTurno(idTurno: Long) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        db.eliminarTurno(idTurno)
        _mensaje.value = "✅ Turno eliminado con éxito"
        recargarTurnos()
    }

    fun insertarTurno(idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Long {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Solo un administrador puede programar turnos"
            return -1L
        }
        val id = db.insertarTurno(idUsuario, acceso, horaInicio, horaFin, fecha)
        if (id != -1L) {
            _mensaje.value = "✅ Turno programado con éxito"
            recargarTurnos()
        }
        return id
    }

    fun actualizarTurno(idTurno: Long, idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Int {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Solo un administrador puede modificar turnos"
            return 0
        }
        val res = db.actualizarTurno(idTurno, idUsuario, acceso, horaInicio, horaFin, fecha)
        if (res > 0) {
            _mensaje.value = "✅ Turno actualizado con éxito"
            recargarTurnos()
        }
        return res
    }

    fun buscarTurnoPorId(idTurno: Long): Turno? = db.buscarTurnoPorId(idTurno)

    fun haySolapamientoTurno(idUsuario: Long, acceso: Int, fecha: String, inicio: String, fin: String, excluirIdTurno: Long = 0L): Boolean =
        db.haySolapamientoTurno(idUsuario, acceso, fecha, inicio, fin, excluirIdTurno)

    fun obtenerTurnoActual(idUsuario: Long): Turno? {
        return db.obtenerTurnoActualObjeto(idUsuario, Fechas.hoy(), Fechas.horaActual())
    }

    override fun onCleared() {
        db.close()
    }
}
