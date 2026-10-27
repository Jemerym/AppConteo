package com.example.appconteo.navigation

sealed class Ruta(val ruta: String) {
    data object Registro : Ruta("registro")
    data object Login : Ruta("login")
    data object Perfil : Ruta("perfil")
    data object Admin : Ruta("admin")
    data object Usuarios : Ruta("usuarios")
    data object InicioUsuarios : Ruta("inicio_usuarios")
    data object EditarPerfil: Ruta("editar_perfil")

    data object DetalleUsuario : Ruta("detalle_usuario/{usuarioId}") {
        fun crear(id: Long) = "detalle_usuario/$id"
    }
}