package com.example.appconteo.navigation

sealed class Ruta(val ruta: String) {
    data object Bienvenido : Ruta("bienvenido")
    data object Login : Ruta("login")
    data object Perfil : Ruta("perfil")
}