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

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    private val _usuarios = MutableStateFlow(db.listarUsuarios())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    val roles: List<Rol> = db.listarRoles()

    private val _usuarioActual = MutableStateFlow(db.buscarPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private fun recargar() {
        val texto = _filtro.value.trim()
        _usuarios.value = if (texto.isEmpty()) db.listarUsuarios() else db.buscarUsuarios(texto)
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        recargar()
    }

    fun actualizar(editado: Usuario) {
        db.actualizarUsuario(editado)
        if (_usuarioActual.value?.id == editado.id) _usuarioActual.value =
            db.buscarPorId(editado.id)
        recargar()
    }

    fun eliminar(usuario: Usuario) {
        db.eliminarUsuario(usuario.id)
        if (_usuarioActual.value?.id == usuario.id) cerrarSesion()
        recargar()
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
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
        _usuarioActual.value = db.buscarPorId(sesion.leer())
    }

    private val _historial = MutableStateFlow(db.listarHistorial())
    val historial: StateFlow<List<HistorialRegistros>> = _historial.asStateFlow()

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

    private val _turnos = MutableStateFlow(db.listarTurnos())
    val turnos: StateFlow<List<Turno>> = _turnos.asStateFlow()

    fun recargarTurnos() {
        _turnos.value = db.listarTurnos()
    }

    fun eliminarTurno(idTurno: Long) {
        db.eliminarTurno(idTurno)
        recargarTurnos()
    }


    fun recuperarClave(correo: String, palabraSecreta: String): String? {
        val usuarioEncontrado = db.buscarPorCorreoYPalabra(correo, palabraSecreta)
        return usuarioEncontrado?.contrasena
    }

    fun buscarPorId(id: Long): Usuario? = db.buscarPorId(id)

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val res = db.actualizarAvatar(id, avatar)
        recargarUsuarioActual()
        return res
    }

    fun existeCorreo(correo: String): Boolean = db.buscarPorCorreo(correo) != null

    fun insertarUsuario(usuario: Usuario): Long {
        val id = db.insertarUsuario(usuario)
        recargar()
        return id
    }

    fun insertarTurno(idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Long {
        val id = db.insertarTurno(idUsuario, acceso, horaInicio, horaFin, fecha)
        recargarTurnos()
        return id
    }

    fun actualizarTurno(idTurno: Long, idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Int {
        val res = db.actualizarTurno(idTurno, idUsuario, acceso, horaInicio, horaFin, fecha)
        recargarTurnos()
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
