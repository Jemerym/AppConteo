package com.example.appconteo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.appconteo.model.HistorialItem
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Usuario

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "miregistro.db"
        private const val VERSION_BD = 7
        private const val USU_CONTRASENA = "contrasena"

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"

        private const val USU_ROL = "id_rol"

        private const val USU_AVATAR = "avatar"

        private const val TABLA_HISTORIAL = "historial_aforo"
        private const val HIST_ID = "id_historial"
        private const val HIST_OBSERVADOR = "observador_nombre"
        private const val HIST_TIPO = "tipo_registro"
        private const val HIST_DETALLE = "detalle"
        private const val HIST_HORA = "hora"

        private const val SQL_CREAR_HISTORIAL =
            "CREATE TABLE $TABLA_HISTORIAL (" +
                    "$HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$HIST_OBSERVADOR TEXT NOT NULL, " +
                    "$HIST_TIPO TEXT NOT NULL, " +
                    "$HIST_DETALLE TEXT NOT NULL, " +
                    "$HIST_HORA TEXT NOT NULL)"
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
                    "$USU_AVATAR BLOB, "+
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(1, 'Administrador'), (2, 'Docente'), (3, 'Estudiante')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_CONTRASENA, $USU_ROL) VALUES " +
                    "('Administrador', 'admin@demo.cl', '1234', 1), " +
                    "('Camila Rojas', 'camila@duocuc.cl', '123456', 2), " +
                    "('Diego Muñoz', 'diego@duocuc.cl', '123456', 2)"

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_HISTORIAL")
        onCreate(db)
    }
    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarUsuarios(texto: String): List<Usuario> {
        val patron = "%$texto%"
        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_NOMBRE LIKE ? OR u.$USU_CORREO LIKE ? " +
                    "ORDER BY u.$USU_NOMBRE",
            arrayOf(patron, patron)
        )
    }

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(
            TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString())
        )

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_CONTRASENA, usuario.contrasena)
        put(USU_ROL, usuario.rolId)
    }

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }

    fun actualizarAvatar(id : Long, avatar: ByteArray) :Int{
        val valores = ContentValues().apply { put(USU_AVATAR, avatar) }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }
    fun insertarHistorial(observador: String, tipo: String, detalle: String, hora: String): Long {
        val valores = ContentValues().apply {
            put(HIST_OBSERVADOR, observador)
            put(HIST_TIPO, tipo)
            put(HIST_DETALLE, detalle)
            put(HIST_HORA, hora)
        }
        return writableDatabase.insert(TABLA_HISTORIAL, null, valores)
    }

    fun listarHistorial(): List<HistorialItem> {
        val lista = mutableListOf<HistorialItem>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_HISTORIAL ORDER BY $HIST_ID DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    HistorialItem(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(HIST_ID)),
                        observadorNombre = cursor.getString(cursor.getColumnIndexOrThrow(HIST_OBSERVADOR)),
                        tipoRegistro = cursor.getString(cursor.getColumnIndexOrThrow(HIST_TIPO)),
                        detalle = cursor.getString(cursor.getColumnIndexOrThrow(HIST_DETALLE)),
                        hora = cursor.getString(cursor.getColumnIndexOrThrow(HIST_HORA))
                    )
                )
            }
        }
        return lista
    }
    private fun leerUsuario(cursor: Cursor) = Usuario(
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),
        contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRASENA)),
        avatar = cursor.getColumnIndexOrThrow(USU_AVATAR).let { columna ->
            if (cursor.isNull(columna)) null else cursor.getBlob(columna)
        }
    )


}
