package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appconteo.data.AvatarRepository
import com.example.appconteo.data.RegistroRepository
import com.example.appconteo.data.SesionPreferencias
import com.example.appconteo.data.TurnoRepository
import com.example.appconteo.data.UsuarioRepository
import com.example.appconteo.data.remote.RegistroAforoRequest
import com.example.appconteo.model.CambiosPerfil
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

    private val repo = UsuarioRepository()
    private val avatarRepo = AvatarRepository()
    private val turnoRepo = TurnoRepository()
    private val registroRepo = RegistroRepository()
    private val sesion = SesionPreferencias(application)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _historial = MutableStateFlow<List<HistorialRegistros>>(emptyList())
    val historial: StateFlow<List<HistorialRegistros>> = _historial.asStateFlow()

    val esAdministrador: Boolean
        get() = _usuarioActual.value?.rolId == Rol.ID_ADMINISTRADOR

    private var accionEnCurso = false

    init {
        val idSesion = sesion.leer()
        _usuarioActual.value = sesion.leerUsuario()
        recargar()
        if (idSesion != -1L) recargarUsuarioActual()
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

    fun recargar() {
        cargarHistorial()
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        if (!esAdministrador) return
        ejecutar {
            val lista = repo.listar(_filtro.value.trim(), incluirAnulados = true)
            _usuarios.value = lista
        }
    }

    fun recargarUsuarioActual() {
        val idSesion = sesion.leer()
        if (idSesion == -1L) {
            _usuarioActual.value = null
            return
        }
        ejecutar(onError = { if (it == "Usuario no encontrado") cerrarSesion() }) {
            val u = repo.obtener(idSesion)
            if (!u.activo) {
                _mensaje.value = "⚠️ Tu usuario fue anulado. Contacta al administrador."
                cerrarSesion()
                return@ejecutar
            }
            _usuarioActual.value = u
            sesion.guardar(u)
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        cargarUsuarios()
    }

    fun limpiar() {
        buscar("")
    }

    fun buscarPorId(id: Long): Usuario? = _usuarios.value.find { it.id == id }

    fun iniciarSesion(correo: String, contrasena: String, onExito: () -> Unit, onError: (String) -> Unit) {
        accion(onError) {
            val usuario = repo.iniciarSesion(correo, contrasena)
            _usuarioActual.value = usuario
            sesion.guardar(usuario)
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
            try {
                repo.iniciarSesion(original.correo, cambios.contrasenaActual)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val sinRed = e.message?.startsWith("No se pudo conectar") == true
                throw if (sinRed) e else Exception("Contraseña actual incorrecta")
            }
            val actualizado = repo.actualizar(original.copy(nombre = cambios.nombre))
            cambios.nuevaContrasena?.takeIf { it.isNotBlank() }?.let { nueva ->
                repo.cambiarContrasena(original.id, cambios.contrasenaActual, nueva)
            }
            _usuarioActual.value = actualizado
            sesion.guardar(actualizado)
            _mensaje.value = "✅ Usuario '${actualizado.nombre}' actualizado"
            onExito()
        }
    }

    fun actualizarAvatar(id: Long, avatar: ByteArray) {
        ejecutar {
            _usuarioActual.value = avatarRepo.subir(id, avatar)
            _mensaje.value = "✅ Foto de perfil actualizada"
        }
    }

    fun anular(usuario: Usuario) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            repo.anular(usuario.id)
            var avisoTurnos = ""
            try {
                turnoRepo.anularDeUsuario(usuario.id)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                avisoTurnos = " (no se pudieron anular sus turnos: ${e.message})"
            }
            _mensaje.value = "✅ Usuario '${usuario.nombre}' anulado$avisoTurnos"
            if (_usuarioActual.value?.id == usuario.id) cerrarSesion() else recargar()
        }
    }

    fun restaurar(usuario: Usuario) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            repo.restaurar(usuario.id)
            _mensaje.value = "✅ Usuario '${usuario.nombre}' restaurado"
            recargar()
        }
    }

    private fun cargarHistorial() {
        val u = _usuarioActual.value ?: return
        ejecutar {
            _historial.value = registroRepo.listar(
                usuarioId = if (u.esAdmin) null else u.id,
                incluirAnulados = true
            )
        }
    }

    fun refrescarHistorial() {
        val u = _usuarioActual.value ?: return
        ejecutar(onError = { }) {
            _historial.value = registroRepo.listar(
                usuarioId = if (u.esAdmin) null else u.id,
                incluirAnulados = true
            )
        }
    }

    fun registrarEnHistorial(
        observadorNombre: String,
        tipo: String,
        acceso: Int = 1,
        idTurno: Long = 0,
        usuarioId: Long = 0,
        tipoGrupo: String = "Solo",
        rangoEtario: String = "25-34",
        codigoBoleta: String? = null,
        onResultado: (Boolean) -> Unit = {}
    ) {
        val datos = RegistroAforoRequest(
            usuarioId = usuarioId,
            observadorNombre = observadorNombre,
            turnoId = if (idTurno > 0) idTurno else null,
            acceso = acceso,
            tipoRegistro = tipo,
            tipoGrupo = tipoGrupo,
            rangoEtario = rangoEtario,
            codigoBoleta = codigoBoleta
        )
        ejecutar(onError = { error ->
            _mensaje.value = "❌ $error"
            onResultado(false)
        }) {
            val nuevo = registroRepo.crear(datos)
            _historial.value = listOf(nuevo) + _historial.value
            onResultado(true)
        }
    }

    fun puedeEditarRegistro(registro: HistorialRegistros, turnoActual: Turno?): Boolean {
        val yo = _usuarioActual.value ?: return false
        if (yo.esAdmin) return true
        return registro.usuarioId == yo.id &&
                turnoActual != null &&
                !turnoActual.anulado &&
                turnoActual.usuarioId == yo.id &&
                registro.idTurno == turnoActual.id
    }

    private fun registroEditable(idHistorial: Long, turnoActual: Turno?): HistorialRegistros? {
        val registro = _historial.value.find { it.id == idHistorial }
        if (registro == null || !puedeEditarRegistro(registro, turnoActual)) {
            _mensaje.value = "⚠️ Solo puedes modificar tus registros mientras tu turno está en curso"
            return null
        }
        return registro
    }

    fun actualizarHistorial(
        idHistorial: Long,
        acceso: Int,
        tipoGrupo: String,
        rangoEtario: String,
        turnoActual: Turno?
    ) {
        val registro = registroEditable(idHistorial, turnoActual) ?: return
        if (registro.anulado) {
            _mensaje.value = "⚠️ El registro está anulado: restáuralo antes de modificarlo"
            return
        }
        val accesoFinal = if (esAdministrador) acceso else registro.acceso
        ejecutar {
            registroRepo.modificar(idHistorial, accesoFinal, tipoGrupo, rangoEtario)
            cargarHistorial()
            _mensaje.value = "✅ Registro actualizado"
        }
    }

    fun anularHistorial(idHistorial: Long, turnoActual: Turno?) {
        registroEditable(idHistorial, turnoActual) ?: return
        ejecutar {
            registroRepo.anular(idHistorial)
            cargarHistorial()
            _mensaje.value = "✅ Registro anulado"
        }
    }

    fun restaurarHistorial(idHistorial: Long, turnoActual: Turno?) {
        registroEditable(idHistorial, turnoActual) ?: return
        ejecutar {
            registroRepo.restaurar(idHistorial)
            cargarHistorial()
            _mensaje.value = "✅ Registro restaurado"
        }
    }
}
