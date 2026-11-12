package com.example.appconteo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.text.SimpleDateFormat
import com.example.appconteo.model.HistorialRegistros
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import java.util.Date
import java.util.Locale

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "miregistro.db"
        private const val VERSION_BD = 16

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_PALABRA_SECRETA = "palabra_secreta"
        private const val USU_ROL = "id_rol"
        private const val USU_AVATAR = "avatar"

        private const val TABLA_HISTORIAL = "historial_aforo"
        private const val HIST_ID = "id_historial"
        private const val HIST_OBSERVADOR = "observador_nombre"
        private const val HIST_TIPO = "tipo_registro"
        private const val HIST_ACCESO = "acceso"
        private const val HIST_TURNO_ID = "id_turno"
        private const val HIST_USUARIO_ID = "id_usuario"
        private const val HIST_GRUPO = "tipo_grupo"
        private const val HIST_RANGO = "rango_etario"
        private const val HIST_DETALLE = "detalle"
        private const val HIST_HORA = "hora"
        private const val HIST_FECHA = "fecha"

        private const val TABLA_TURNOS = "turnos"
        private const val TURNO_ID = "id_turno"
        private const val TURNO_USUARIO = "id_usuario"
        private const val TURNO_ACCESO = "acceso"
        private const val TURNO_INICIO = "hora_inicio"
        private const val TURNO_FIN = "hora_fin"
        private const val TURNO_FECHA = "fecha"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL, " +
                    "$USU_CONTRASENA TEXT NOT NULL DEFAULT '', " +
                    "$USU_PALABRA_SECRETA TEXT NOT NULL DEFAULT '', " +
                    "$USU_AVATAR BLOB, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_CREAR_HISTORIAL =
            "CREATE TABLE $TABLA_HISTORIAL (" +
                    "$HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$HIST_OBSERVADOR TEXT NOT NULL, " +
                    "$HIST_TIPO TEXT NOT NULL, " +
                    "$HIST_ACCESO INTEGER NOT NULL DEFAULT 1, " +
                    "$HIST_TURNO_ID INTEGER REFERENCES $TABLA_TURNOS($TURNO_ID) ON DELETE SET NULL, " +
                    "$HIST_USUARIO_ID INTEGER REFERENCES $TABLA_USUARIOS($USU_ID) ON DELETE SET NULL, " +
                    "$HIST_GRUPO TEXT NOT NULL DEFAULT 'Solo', " +
                    "$HIST_RANGO TEXT NOT NULL DEFAULT '25-34', " +
                    "$HIST_DETALLE TEXT NOT NULL, " +
                    "$HIST_FECHA TEXT NOT NULL, " +
                    "$HIST_HORA TEXT NOT NULL)"

        private const val SQL_CREAR_TURNOS =
            "CREATE TABLE $TABLA_TURNOS (" +
                    "$TURNO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TURNO_USUARIO INTEGER NOT NULL, " +
                    "$TURNO_ACCESO INTEGER NOT NULL, " +
                    "$TURNO_INICIO TEXT NOT NULL, " +
                    "$TURNO_FIN TEXT NOT NULL, " +
                    "$TURNO_FECHA TEXT NOT NULL, " +
                    "FOREIGN KEY($TURNO_USUARIO) REFERENCES $TABLA_USUARIOS($USU_ID) ON DELETE CASCADE)"

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(1, 'Administrador'), (2, 'Observador')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_PALABRA_SECRETA, $USU_ROL) VALUES " +
                    "('Administrador', 'admin@demo.cl', '1234', 'duoc', 1), " +
                    "('Camila Rojas', 'camila@duocuc.cl', '123456', 'duoc', 2), " +
                    "('Diego Muñoz', 'diego@duocuc.cl', '123456', 'duoc', 2)"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_CREAR_HISTORIAL)
        db.execSQL(SQL_CREAR_TURNOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_TURNOS")
        onCreate(db)
    }
    private fun Cursor.entero(col: String): Int = getInt(getColumnIndexOrThrow(col))
    private fun Cursor.largo(col: String): Long = getLong(getColumnIndexOrThrow(col))
    private fun Cursor.texto(col: String): String = getString(getColumnIndexOrThrow(col))
    private fun Cursor.textoONulo(col: String): String? {
        val i = getColumnIndexOrThrow(col)
        return if (isNull(i)) null else getString(i)
    }
    private fun Cursor.blobONulo(col: String): ByteArray? {
        val i = getColumnIndexOrThrow(col)
        return if (isNull(i)) null else getBlob(i)
    }
    private fun <T> consultar(sql: String, args: Array<String>? = null, mapeador: (Cursor) -> T): List<T> {
        val lista = mutableListOf<T>()
        readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                lista.add(mapeador(c))
            }
        }
        return lista
    }
    private fun leerRol(c: Cursor) = Rol(
        id = c.largo(ROL_ID),
        nombre = c.texto(ROL_NOMBRE)
    )

    private fun leerUsuario(c: Cursor) = Usuario(
        id = c.largo(USU_ID),
        nombre = c.texto(USU_NOMBRE),
        correo = c.texto(USU_CORREO),
        contrasena = c.texto(USU_CONTRASENA),
        palabraSecreta = c.textoONulo(USU_PALABRA_SECRETA) ?: "",
        rolId = c.largo(USU_ROL),
        rolNombre = c.texto(ROL_NOMBRE),
        avatar = c.blobONulo(USU_AVATAR)
    )

    private fun leerHistorial(c: Cursor) = HistorialRegistros(
        id = c.largo(HIST_ID),
        observadorNombre = c.texto(HIST_OBSERVADOR),
        tipoRegistro = c.texto(HIST_TIPO),
        acceso = c.entero(HIST_ACCESO),
        idTurno = c.largo(HIST_TURNO_ID),
        usuarioId = c.largo(HIST_USUARIO_ID),
        tipoGrupo = c.texto(HIST_GRUPO),
        rangoEtario = c.texto(HIST_RANGO),
        fecha = c.texto(HIST_FECHA),
        hora = c.texto(HIST_HORA)
    )

    private fun leerTurno(c: Cursor) = Turno(
        id = c.largo(TURNO_ID),
        usuarioId = c.largo(TURNO_USUARIO),
        observadorNombre = c.texto("observador_nombre"),
        acceso = c.entero(TURNO_ACCESO),
        horaInicio = c.texto(TURNO_INICIO),
        horaFin = c.texto(TURNO_FIN),
        fecha = c.texto(TURNO_FECHA)
    )

    // --- OPERACIONES DE USUARIOS ---
    fun insertarUsuario(usuario: Usuario): Long =
        writableDatabase.insert(TABLA_USUARIOS, null, valoresDe(usuario))

    fun listarRoles(): List<Rol> =
        consultar("SELECT $ROL_ID, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null, ::leerRol)

    fun listarUsuarios(): List<Usuario> =
        consultar("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null, ::leerUsuario)

    fun buscarUsuarios(texto: String): List<Usuario> {
        val patron = "%$texto%"
        return consultar(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_NOMBRE LIKE ? OR u.$USU_CORREO LIKE ? ORDER BY u.$USU_NOMBRE",
            arrayOf(patron, patron),
            ::leerUsuario
        )
    }

    fun buscarPorId(id: Long): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()), ::leerUsuario).firstOrNull()

    fun buscarPorCorreo(correo: String): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ?", arrayOf(correo.trim()), ::leerUsuario).firstOrNull()

    fun buscarPorCorreoYPalabra(correo: String, palabraSecreta: String): Usuario? =
        consultar(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ? AND u.$USU_PALABRA_SECRETA = ?",
            arrayOf(correo.trim(), palabraSecreta.trim()),
            ::leerUsuario
        ).firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString()))

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val valores = ContentValues().apply { put(USU_AVATAR, avatar) }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_CONTRASENA, usuario.contrasena)
        put(USU_PALABRA_SECRETA, usuario.palabraSecreta)
        put(USU_ROL, usuario.rolId)
    }
    fun insertarHistorial(observador: String, tipo: String, acceso: Int = 1, idTurno: Long = 0, usuarioId: Long = 0, tipoGrupo: String = "Solo", rangoEtario: String = "25-34", hora: String
    ): Long {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val detalle = "Tipo: $tipoGrupo, Edad: $rangoEtario"
        val valores = ContentValues().apply {
            put(HIST_OBSERVADOR, observador)
            put(HIST_TIPO, tipo)
            put(HIST_ACCESO, acceso)
            put(HIST_TURNO_ID, if (idTurno > 0) idTurno else null)
            put(HIST_USUARIO_ID, if (usuarioId > 0) usuarioId else null)
            put(HIST_GRUPO, tipoGrupo)
            put(HIST_RANGO, rangoEtario)
            put(HIST_DETALLE, detalle)
            put(HIST_FECHA, fechaActual)
            put(HIST_HORA, hora)
        }
        return writableDatabase.insert(TABLA_HISTORIAL, null, valores)
    }

    fun listarHistorial(): List<HistorialRegistros> =
        consultar("SELECT * FROM $TABLA_HISTORIAL ORDER BY $HIST_ID DESC", null, ::leerHistorial)

    fun listarHistorialPorObservador(
        nombreObservador: String,
        fechaFiltro: String? = null
    ): List<HistorialRegistros> {
        val where = StringBuilder("$HIST_OBSERVADOR = ?")
        val args = mutableListOf(nombreObservador)
        if (!fechaFiltro.isNullOrBlank()) {
            where.append(" AND $HIST_FECHA = ?")
            args.add(fechaFiltro)
        }
        val query = "SELECT * FROM $TABLA_HISTORIAL WHERE $where ORDER BY $HIST_ID DESC LIMIT 15 OFFSET 15"
        return consultar(query, args.toTypedArray(), ::leerHistorial)
    }

    fun insertarTurno(idUsuario: Long, acceso: Int, horaInicio: String, horaFin: String, fecha: String): Long {
        val valores = ContentValues().apply {
            put(TURNO_USUARIO, idUsuario)
            put(TURNO_ACCESO, acceso)
            put(TURNO_INICIO, horaInicio)
            put(TURNO_FIN, horaFin)
            put(TURNO_FECHA, fecha)
        }
        return try {
            writableDatabase.insert(TABLA_TURNOS, null, valores)
        } catch (_: Exception) {
            -1L
        }
    }

    fun haySolapamientoTurno(idUsuario: Long, acceso: Int, fecha: String, inicio: String, fin: String): Boolean {
        val sql = """
            SELECT 1 FROM $TABLA_TURNOS 
            WHERE $TURNO_FECHA = ? 
            AND $TURNO_INICIO < ? AND $TURNO_FIN > ? 
            AND ($TURNO_USUARIO = ? OR $TURNO_ACCESO = ?)
            LIMIT 1
        """.trimIndent()

        return consultar(sql, arrayOf(fecha, fin, inicio, idUsuario.toString(), acceso.toString())) { true }.isNotEmpty()
    }

    fun listarTurnos(): List<Turno> {
        val sql = """
            SELECT t.*, u.$USU_NOMBRE AS observador_nombre 
            FROM $TABLA_TURNOS t
            INNER JOIN $TABLA_USUARIOS u ON t.$TURNO_USUARIO = u.$USU_ID
            ORDER BY t.$TURNO_ID DESC
        """.trimIndent()
        return consultar(sql, null, ::leerTurno)
    }

    fun obtenerAccesoActualObservador(idUsuario: Long, horaActual: String): Int {
        val sql = "SELECT $TURNO_ACCESO FROM $TABLA_TURNOS WHERE $TURNO_USUARIO = ? AND ? >= $TURNO_INICIO AND ? <= $TURNO_FIN"
        val resultados = consultar(sql, arrayOf(idUsuario.toString(), horaActual, horaActual)) { c -> c.entero(TURNO_ACCESO) }
        return resultados.firstOrNull() ?: -1
    }
}
