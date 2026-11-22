package com.example.appconteo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = PrimarioClaro,
    onPrimaryContainer = PrimarioOscuro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = PrimarioClaro,
    onPrimary = PrimarioOscuro,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = PrimarioClaro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro
)

// Temas personalizables: cambia estos colores a tu gusto.
private val EsquemaTema1 = darkColorScheme(
    primary = TokyoAzulPrimario,
    onPrimary = TokyoFondo,
    primaryContainer = TokyoAzulContenedor,
    onPrimaryContainer = TokyoTextoClaro,
    secondary = TokyoPurpuraSecundario,
    onSecondary = TokyoFondo,
    tertiary = TokyoCianTerciario,
    onTertiary = TokyoFondo,
    background = TokyoFondo,
    onBackground = TokyoTextoClaro,
    surface = TokyoSuperficie,
    onSurface = TokyoTextoClaro,
    surfaceVariant = TokyoAccesoNoSel,          // tarjetas de acceso no seleccionadas
    onSurfaceVariant = TokyoTextoSecundario,
    secondaryContainer = TokyoChipHoy,          // chip "Hoy" o estado secundario
    onSecondaryContainer = TokyoTextoClaro,
    tertiaryContainer = TokyoResumen,           // tarjeta de resumen
    onTertiaryContainer = TokyoTextoClaro,
    outline = TokyoBorde                        // borde de los campos
)


// Tema 2: paleta Morado / Ciruela Oscuro (Color.kt)
private val EsquemaTema2 = darkColorScheme(
    primary = MoradoPrimario,
    onPrimary = MoradoTextoClaro,
    primaryContainer = MoradoContenedor,
    onPrimaryContainer = MoradoTextoClaro,
    secondary = MoradoAcentoTexto,
    onSecondary = MoradoFondo,
    tertiary = SalmoAlertaTexto,
    onTertiary = MoradoFondo,
    background = MoradoFondo,
    onBackground = MoradoTextoClaro,
    surface = MoradoSuperficie,
    onSurface = MoradoTextoClaro,
    surfaceVariant = MoradoAccesoNoSel,
    onSurfaceVariant = MoradoTextoClaro,
    secondaryContainer = MoradoChipHoy,
    onSecondaryContainer = MoradoTextoClaro,
    tertiaryContainer = MoradoResumen,
    onTertiaryContainer = MoradoTextoClaro,
    outline = MoradoTextoClaro
)
private val EsquemaTema3 = lightColorScheme(
    primary = LogoClaroDoradoPrimario,              // Botones principales, títulos activos y bordes seleccionados
    onPrimary = LogoClaroSuperficie,                // Texto blanco sobre botón dorado
    primaryContainer = LogoClaroDoradoContenedor,   // Fondo de tarjeta seleccionada (en tu selector de accesos/temas)
    onPrimaryContainer = LogoClaroDoradoOscuro,     // Texto dentro de tarjeta seleccionada
    secondary = LogoClaroAzulSecundario,            // Azul marino para iconos y acciones secundarias
    onSecondary = LogoClaroSuperficie,
    tertiary = LogoClaroDoradoPrimario,
    onTertiary = LogoClaroSuperficie,
    background = LogoClaroFondo,
    onBackground = LogoClaroTextoOscuro,
    surface = LogoClaroSuperficie,
    onSurface = LogoClaroTextoOscuro,
    surfaceContainerLow = LogoClaroSuperficie,      // Menú lateral blanco limpio
    surfaceVariant = LogoClaroAccesoNoSel,          // Tarjetas de acceso no seleccionadas (crema suave)
    onSurfaceVariant = LogoClaroTextoSecundario,
    secondaryContainer = LogoClaroAzulContenedor,   // Chip "Hoy" en azul claro
    onSecondaryContainer = LogoClaroAzulSecundario,
    tertiaryContainer = LogoClaroResumen,           // Tarjeta de resumen en azul marino del logo
    onTertiaryContainer = LogoClaroSuperficie,      // Texto blanco sobre la tarjeta azul marino
    outline = LogoClaroBorde                        // Bordes de los campos en dorado suave
)
private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

// Elige los colores según el tema que el usuario marcó en Perfil > "Tema".
@Composable
fun esquemaDe(tema: TemaApp): ColorScheme = when (tema) {
    TemaApp.SISTEMA -> if (isSystemInDarkTheme()) EsquemaOscuro else EsquemaClaro
    TemaApp.CLARO -> EsquemaClaro
    TemaApp.OSCURO -> EsquemaOscuro
    TemaApp.TEMA1 -> EsquemaTema1
    TemaApp.TEMA2 -> EsquemaTema2
    TemaApp.TEMA3 -> EsquemaTema3
}

// La usa MainActivity con el tema guardado.
@Composable
fun AppConteoTheme(
    tema: TemaApp,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = esquemaDe(tema),
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}

// Se mantiene para los @Preview: AppConteoTheme { ... } y AppConteoTheme(darkTheme = true) { ... }
@Composable
fun AppConteoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AppConteoTheme(tema = if (darkTheme) TemaApp.OSCURO else TemaApp.CLARO, content = content)
}
