package com.example.appconteo.navigation

sealed class Ruta(val ruta: String) {
    data object Registro : Ruta("registro")
    data object Login : Ruta("login")
    data object Perfil : Ruta("perfil")
    data object Admin : Ruta("admin")
    data object Usuarios : Ruta("usuarios")
    data object InicioUsuarios : Ruta("inicio_usuarios")
    data object EditarPerfil: Ruta("editar_perfil")
    data object MisTurnos : Ruta("mis_turnos")
    data object EscanearQr : Ruta("escanear_qr")
    data object RecuperarClave : Ruta("recuperar_clave")

    data object ProgramarSesion : Ruta("programar_sesion/{turnoId}") {
        fun crear(turnoId: Long = 0L) = "programar_sesion/$turnoId"
    }

    data object VerRegistros : Ruta("ver_registros?turnoId={turnoId}") {
        fun crear(turnoId: Long = 0L) = "ver_registros?turnoId=$turnoId"
    }

    data object DetalleUsuario : Ruta("detalle_usuario/{usuarioId}") {
        fun crear(id: Long) = "detalle_usuario/$id"
    }
}
