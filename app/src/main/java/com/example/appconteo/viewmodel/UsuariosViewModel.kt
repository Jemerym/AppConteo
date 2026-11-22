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

    // USUARIOS -> ahora vienen del microservicio usuarios-service (UsuarioRepository).
    private val repo = UsuarioRepository()
    private val avatarRepo = AvatarRepository()
    private val turnoRepo = TurnoRepository()   // solo para anular sus turnos al anular un usuario
    private val registroRepo = RegistroRepository()   // REGISTROS -> ahora vienen de registros-service

    // La sesión (quién está conectado) se guarda en el teléfono con SharedPreferences.
    private val sesion = SesionPreferencias(application)

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    // Incluye anulados: la pantalla decide si mostrarlos (chip "Mostrar anulados").
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _historial = MutableStateFlow<List<HistorialRegistros>>(emptyList())
    val historial: StateFlow<List<HistorialRegistros>> = _historial.asStateFlow()

    val esAdministrador: Boolean
        get() = _usuarioActual.value?.rolId == Rol.ID_ADMINISTRADOR

    // Evita enviar dos veces la misma acción si el usuario toca el botón repetido.
    private var accionEnCurso = false

    init {
        // El usuario de la sesión se lee de SesionPreferencias para decidir la pantalla inicial al instante;
        // luego se refresca desde el servidor.
        val idSesion = sesion.leer()
        _usuarioActual.value = sesion.leerUsuario()
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

    // ---------- carga de datos ----------

    fun recargar() {
        cargarHistorial()
        cargarUsuarios()
    }

    // Solo el administrador necesita la lista completa de usuarios.
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
        // En silencio: si no hay red se conserva el usuario que ya está en pantalla.
        ejecutar(onError = { if (it == "Usuario no encontrado") cerrarSesion() }) {
            val u = repo.obtener(idSesion)
            if (!u.activo) {
                // Lo anularon mientras tenía la sesión abierta: se cierra (la navegación vuelve al Login).
                _mensaje.value = "⚠️ Tu usuario fue anulado. Contacta al administrador."
                cerrarSesion()
                return@ejecutar
            }
            _usuarioActual.value = u
            sesion.guardar(u)   // mantiene al día nombre y rol guardados
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        cargarUsuarios()
    }

    // Deja la pantalla Usuarios como nueva: buscador vacío y lista completa (igual que limpiar() de LoginViewModel).
    // Se llama al salir de ListaUsuarios, así el texto buscado no queda filtrando la lista en Programar.
    fun limpiar() {
        buscar("")
    }

    // Usado por ProgramarScreen (modo edición). Busca en la lista ya descargada.
    fun buscarPorId(id: Long): Usuario? = _usuarios.value.find { it.id == id }

    // ---------- sesión y cuenta ----------

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

    // CAMBIO: antes se eliminaba. Ahora se ANULA: el usuario no puede entrar, pero se conserva
    // (con su historial) y el administrador lo puede restaurar.
    fun anular(usuario: Usuario) {
        if (!esAdministrador) {
            _mensaje.value = "⚠️ Acción no permitida"
            return
        }
        ejecutar {
            repo.anular(usuario.id)
            // Sus turnos viven en otro servicio: se anulan aparte. Al restaurar al usuario NO se restauran
            // solos (el admin decide cuáles reactivar), porque el tramo pudo asignarse a otra persona.
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

    // ---------- HISTORIAL (ahora en registros-service) ----------

    // El admin ve todos los registros; el observador solo los suyos. Se traen también los anulados
    // porque Ver Registros tiene el filtro "Mostrar anulados".
    private fun cargarHistorial() {
        val u = _usuarioActual.value ?: return
        ejecutar {
            _historial.value = registroRepo.listar(
                usuarioId = if (u.esAdmin) null else u.id,
                incluirAnulados = true
            )
        }
    }

    // Para el refresco automático cada 15 s: igual que cargarHistorial(), pero sin Toast si falla.
    fun refrescarHistorial() {
        val u = _usuarioActual.value ?: return
        ejecutar(onError = { }) {
            _historial.value = registroRepo.listar(
                usuarioId = if (u.esAdmin) null else u.id,
                incluirAnulados = true
            )
        }
    }

    // CAMBIO: antes devolvía Boolean al instante (SQLite). Ahora el servidor tarda en responder,
    // así que avisa con onResultado(true/false). La fecha y la hora las pone el servidor.
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
            _mensaje.value = "❌ $error"        // ej: "Esta boleta ya fue registrada"
            onResultado(false)
        }) {
            val nuevo = registroRepo.crear(datos)
            _historial.value = listOf(nuevo) + _historial.value
            onResultado(true)
        }
    }

    /**
     * Quién puede modificar, anular o restaurar un registro:
     *  - el administrador, siempre;
     *  - el observador, solo SUS registros y solo mientras el turno en que los hizo sigue en curso
     *    (turnoActual = turno activo según el servidor, TurnosViewModel.turnoActual).
     * La pantalla usa esta misma función para mostrar u ocultar los botones.
     */
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
        // El observador no cambia la puerta: el registro queda en el acceso de su turno.
        val accesoFinal = if (esAdministrador) acceso else registro.acceso
        ejecutar {
            registroRepo.modificar(idHistorial, accesoFinal, tipoGrupo, rangoEtario)
            cargarHistorial()
            _mensaje.value = "✅ Registro actualizado"
        }
    }

    // CAMBIO: antes se borraba el registro. Ahora se ANULA (deja de contar) y se puede restaurar.
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
        // Si su boleta ya está en otro registro vigente, el servidor responde 409 con ese mensaje.
        ejecutar {
            registroRepo.restaurar(idHistorial)
            cargarHistorial()
            _mensaje.value = "✅ Registro restaurado"
        }
    }
}