package com.example.appconteo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appconteo.data.SesionPreferencias
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import com.example.appconteo.model.Utilidades
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        recargar()
    }

    fun recargar() {
        val idSesion = sesion.leer()
        _usuarioActual.value = if (idSesion != -1L) db.buscarPorId(idSesion) else null

        val textoFiltro = _filtro.value.trim()
        _usuarios.value = if (textoFiltro.isEmpty()) db.listarUsuarios() else db.buscarUsuarios(textoFiltro)
        _historial.value = db.listarHistorial()
        _turnos.value = db.listarTurnos()
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        recargar()
    }

    fun actualizar(editado: Usuario) {
        val filas = db.actualizarUsuario(editado)
        if (filas > 0) {
            _mensaje.value = "✅ Usuario '${editado.nombre}' actualizado"
            recargar()
        }
    }

    fun eliminar(usuario: Usuario) {
        val filas = db.eliminarUsuario(usuario.id)
        if (filas > 0) {
            _mensaje.value = "✅ Usuario '${usuario.nombre}' eliminado"
            if (_usuarioActual.value?.id == usuario.id) cerrarSesion() else recargar()
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
        recargar()
    }

    fun iniciarSesion(correo: String, contrasena: String): Boolean {
        val usuarioEncontrado = db.buscarPorCorreo(correo)
        val hashInput = Utilidades.hashContrasena(contrasena)
        if (usuarioEncontrado != null && (usuarioEncontrado.contrasena == contrasena || usuarioEncontrado.contrasena == hashInput)) {
            _usuarioActual.value = usuarioEncontrado
            sesion.guardar(usuarioEncontrado.id)
            recargar()
            return true
        }
        return false
    }

    fun recargarUsuarioActual() {
        val idSesion = sesion.leer()
        _usuarioActual.value = if (idSesion != -1L) db.buscarPorId(idSesion) else null
    }

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
        db.actualizarHistorial(idHistorial, acceso, tipoGrupo, rangoEtario)
        _historial.value = db.listarHistorial()
    }

    fun eliminarHistorial(idHistorial: Long) {
        db.eliminarHistorial(idHistorial)
        _historial.value = db.listarHistorial()
    }

    fun recargarTurnos() {
        _turnos.value = db.listarTurnos()
    }

    fun eliminarTurno(idTurno: Long) {
        db.eliminarTurno(idTurno)
        _mensaje.value = "✅ Turno eliminado con éxito"
        recargarTurnos()
    }

    fun recuperarClave(correo: String, palabraSecreta: String): String? {
        val usuarioEncontrado = db.buscarPorCorreoYPalabra(correo, palabraSecreta)
        return usuarioEncontrado?.contrasena
    }

    fun buscarPorId(id: Long): Usuario? = db.buscarPorId(id)

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val res = db.actualizarAvatar(id, avatar)
        if (res > 0) {
            _mensaje.value = "✅ Foto de perfil actualizada"
            recargarUsuarioActual()
        }
        return res
    }

    fun existeCorreo(correo: String): Boolean = db.buscarPorCorreo(correo) != null

    fun insertarUsuario(usuario: Usuario): Long {
        val id = db.insertarUsuario(usuario)
        if (id != -1L) {
            _mensaje.value = "✅ Usuario registrado con éxito"
            recargar()
        }
        return id
    }

    fun insertarTurno(idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Long {
        val id = db.insertarTurno(idUsuario, acceso, horaInicio, horaFin, fecha)
        if (id != -1L) {
            _mensaje.value = "✅ Turno programado con éxito"
            recargarTurnos()
        }
        return id
    }

    fun actualizarTurno(idTurno: Long, idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Int {
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

    fun listarHistorialPorUsuarioPaginado(usuarioId: Long, fechaFiltro: String? = null, limite: Int = 15): List<HistorialRegistros> =
        db.listarHistorialPorUsuarioPaginado(usuarioId, fechaFiltro, limite)

    fun obtenerTurnoActual(idUsuario: Long): Turno? {
        return db.obtenerTurnoActualObjeto(idUsuario, Fechas.hoy(), Fechas.horaActual())
    }
}
