package com.example.appconteo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Sesion
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTurnosScreen(
    usuario: Usuario?,
    turnos: List<Turno>,
    onAnularTurno: (Long) -> Unit = {},
    onRestaurarTurno: (Long) -> Unit = {},
    onRecargarTurnos: () -> Unit = {},
    // Texto a mostrar cuando el servidor no respondió y la lista sale de la copia guardada en el teléfono
    onVerRegistrosTurno: (Long) -> Unit = {},
    onEditarTurno: (Long) -> Unit = {},
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRecargarTurnos()
    }

    var modoAdmin by remember { mutableStateOf(false) }
    var turnoParaAnular by remember { mutableStateOf<Turno?>(null) }
    var mostrarAnulados by remember { mutableStateOf(false) }

    val esAdmin = usuario?.esAdmin == true

    // CAMBIO: los turnos anulados se conservan. El admin puede verlos (y restaurarlos);
    // el observador nunca ve sus turnos anulados.
    val turnosFiltrados = remember(turnos, usuario, esAdmin, mostrarAnulados) {
        if (esAdmin) {
            turnos.filter { mostrarAnulados || !it.anulado }
        } else {
            val idActual = usuario?.id ?: -1L
            turnos.filter { it.usuarioId == idActual && !it.anulado }
        }
    }
    val cantidadAnulados = remember(turnos) { turnos.count { it.anulado } }

    val fechaHoy = remember { Fechas.hoy() }
    val horaActual = remember { Fechas.horaActual() }
    // Orden por fecha y hora (antes salían en el orden en que se asignaron).
    // Primero los que vienen o están en curso: del día más cercano y la hora más temprana.
    // Al final los ya realizados, del más reciente al más antiguo.
    // Orden: primero TODOS los turnos de hoy (por hora), luego los próximos días
    // (del más cercano al más lejano) y al final los días pasados (del más reciente al más antiguo).
    val turnosOrdenados = remember(turnosFiltrados, fechaHoy) {
        val deHoy = turnosFiltrados.filter { it.fecha == fechaHoy }
            .sortedBy { it.horaInicio }
        val proximos = turnosFiltrados.filter { it.fecha > fechaHoy }
            .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))
        val pasados = turnosFiltrados.filter { it.fecha < fechaHoy }
            .sortedWith(compareByDescending<Turno> { it.fecha }.thenByDescending { it.horaInicio })
        deHoy + proximos + pasados
    }
    // Agrupa por día, respetando el orden anterior (groupBy conserva el orden en que aparecen)
    val turnosPorDia = remember(turnosOrdenados) { turnosOrdenados.groupBy { it.fecha } }

    fun calcularEstado(turno: Turno): Sesion {
        return when {
            turno.fecha > fechaHoy -> Sesion.PROGRAMADA
            turno.fecha < fechaHoy -> Sesion.REALIZADA
            else -> {
                when {
                    horaActual < turno.horaInicio -> Sesion.PROGRAMADA
                    horaActual in turno.horaInicio..turno.horaFin -> Sesion.EN_CURSO
                    else -> Sesion.REALIZADA
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esAdmin) "Turnos Programados (Admin)" else "Mis Turnos Asignados", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                    }
                },
                actions = {
                    if (esAdmin) {
                        IconButton(onClick = { modoAdmin = !modoAdmin }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_editar),
                                contentDescription = "Activar modo gestión",
                                tint = if (modoAdmin) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.espacioPantalla)
        ) {

            if (esAdmin) {
                FilterChip(
                    selected = mostrarAnulados,
                    onClick = { mostrarAnulados = !mostrarAnulados },
                    label = { Text("Mostrar anulados ($cantidadAnulados)") },
                    modifier = Modifier.padding(bottom = Dimens.espacioChico)
                )
            }
            if (turnosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay turnos programados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    turnosPorDia.forEach { (fecha, turnosDelDia) ->
                        item(key = "dia-$fecha") {
                            Text(
                                text = tituloDia(fecha, fechaHoy),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = Dimens.espacioChico)
                            )
                        }
                        items(turnosDelDia, key = { it.id }) { turno ->
                            val estado = calcularEstado(turno)
                            val colorEstado = when {
                                turno.anulado -> MaterialTheme.colorScheme.error
                                estado == Sesion.PROGRAMADA -> MaterialTheme.colorScheme.tertiary
                                estado == Sesion.EN_CURSO -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                            val accesosNombres = remember { CatalogosRincon.accesosInfo.toMap() }
                            val nombrePuerta = accesosNombres[turno.acceso] ?: "Norte"

                            TarjetaSeccion(
                                titulo = "Acceso $nombrePuerta • ${turno.observadorNombre}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (turno.anulado) 0.55f else 1f)
                                    .clickable { onVerRegistrosTurno(turno.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Fecha: ${turno.fecha}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Horario: ${turno.horaInicio} a ${turno.horaFin}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Toca la tarjeta para ver registros",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = colorEstado.copy(alpha = 0.2f),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = if (turno.anulado) "Anulado" else estado.etiqueta,
                                                color = colorEstado,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                )
                                            )
                                        }

                                        if (esAdmin && turno.anulado) {
                                            TextButton(onClick = { onRestaurarTurno(turno.id) }) {
                                                Text("Restaurar")
                                            }
                                        } else if (esAdmin && modoAdmin) {
                                            IconButton(onClick = { onEditarTurno(turno.id) }) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_editar),
                                                    contentDescription = "Editar Turno",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { turnoParaAnular = turno }) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_eliminar),
                                                    contentDescription = "Anular Turno",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para ANULAR turno (se conserva y se puede restaurar)
    turnoParaAnular?.let { turno ->
        AlertDialog(
            onDismissRequest = { turnoParaAnular = null },
            title = { Text("Anular Turno") },
            text = { Text("¿Anular el turno de ${turno.observadorNombre} (${turno.fecha} - ${turno.horaInicio} a ${turno.horaFin})? Se libera el horario y podrás restaurarlo después.") },
            confirmButton = {
                TextButton(onClick = {
                    onAnularTurno(turno.id)
                    turnoParaAnular = null
                }) {
                    Text("Anular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { turnoParaAnular = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MisTurnosObservadorPreview() {
    AppConteoTheme {
        MisTurnosScreen(
            usuario = Usuario(id = 2, nombre = "Camila Rojas", correo = "camila@duocuc.cl", rolId = Rol.ID_OBSERVADOR, rolNombre = "Observador"),
            turnos = listOf(
                Turno(id = 1, usuarioId = 2, observadorNombre = "Camila Rojas", acceso = 1, horaInicio = "08:00", horaFin = "16:00", fecha = Fechas.hoy()),
                Turno(id = 2, usuarioId = 2, observadorNombre = "Camila Rojas", acceso = 2, horaInicio = "01:00", horaFin = "23:55", fecha = Fechas.conOffset(0))
            ),
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Turnos - Admin Modo Gestión")
@Composable
private fun MisTurnosAdminPreview() {
    AppConteoTheme {
        MisTurnosScreen(
            usuario = Usuario(id = 1, nombre = "Administrador", correo = "admin@demo.cl", rolId = Rol.ID_ADMINISTRADOR, rolNombre = "Administrador"),
            turnos = listOf(
                Turno(id = 1, usuarioId = 2, observadorNombre = "Camila Rojas", acceso = 1, horaInicio = "08:00", horaFin = "16:00", fecha = Fechas.hoy()),
                Turno(id = 2, usuarioId = 3, observadorNombre = "Diego Muñoz", acceso = 2, horaInicio = "09:00", horaFin = "17:00", fecha = Fechas.hoy())
            ),
            onVolver = {}
        )
    }
}// "Hoy", "Mañana", "Ayer" o la fecha con el día de la semana (ej: "Lunes 24/11/2026")
private fun tituloDia(fecha: String, hoy: String): String {
    val formatoEntrada = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val dia = try { formatoEntrada.parse(fecha) } catch (e: Exception) { null } ?: return fecha
    return when (fecha) {
        hoy -> "Hoy"
        Fechas.conOffset(1) -> "Mañana"
        Fechas.conOffset(-1) -> "Ayer"
        else -> java.text.SimpleDateFormat("EEEE dd/MM/yyyy", java.util.Locale("es", "CL"))
            .format(dia)
            .replaceFirstChar { it.uppercase() }
    }
}
