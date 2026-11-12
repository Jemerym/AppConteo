package com.example.appconteo.model

enum class Clasificacion(val etiqueta: String) {
    TRANSITO("Tránsito"),
    CLIENTE("Cliente")
}

enum class RangoEtario(val etiqueta: String) {
    MENOR_18("< 18"),
    EDAD_18_24("18-24"),
    EDAD_25_34("25-34"),
    EDAD_35_50("35-50"),
    MAS_50("+ 50")
}

enum class TipoCompania(val etiqueta: String) {
    SOLO("Solo"),
    ACOMPANADO("Acompañado (2)"),
    GRUPO_3_MAS("Grupo (3+)")
}

enum class Sesion(val etiqueta: String) {
    PROGRAMADA("Programada"),
    EN_CURSO("En curso"),
    REALIZADA("Realizada")
}

object CatalogosRincon {
    val accesosInfo = listOf(
        1 to "Norte",
        2 to "Sur",
        3 to "Este",
        4 to "Oeste"
    )

    val tramosHorarios = listOf(
        "10:00" to "10:30",
        "10:30" to "11:00",
        "11:00" to "11:30",
        "11:30" to "12:00",
        "12:00" to "12:30",
        "12:30" to "13:00",
        "13:00" to "13:30",
        "13:30" to "14:00",
        "14:00" to "14:30",
        "14:30" to "15:00",
        "15:00" to "15:30",
        "15:30" to "16:00",
        "16:00" to "16:30",
        "16:30" to "17:00",
        "17:00" to "17:30",
        "17:30" to "18:00",
        "18:00" to "18:30",
        "18:30" to "19:00",
        "19:00" to "19:30",
        "19:30" to "20:00",
        "20:00" to "20:30"
    )
}
