package com.example.appconteo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.data.UsuarioDbHelper
import com.example.appconteo.model.CatalogosRincon
import com.example.appconteo.model.Fechas
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Sesion
import com.example.appconteo.model.Turno
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisTurnosScreen(
    usuario: Usuario?,
    turnos: List<Turno>,
    usuariosViewModel: UsuariosViewModel? = null,
    onRecargarTurnos: () -> Unit = {},
    onVerRegistrosTurno: (Long) -> Unit = {},
    onEditarTurno: (Long) -> Unit = {},
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRecargarTurnos()
    }

    val context = LocalContext.current
    var modoAdmin by remember { mutableStateOf(false) }
    var turnoParaEliminar by remember { mutableStateOf<Turno?>(null) }

    val esAdmin = usuario?.rolId == Rol.ID_ADMINISTRADOR

    val turnosFiltrados = remember(turnos, usuario, esAdmin) {
        if (esAdmin) {
            turnos
        } else {
            val nombreActual = usuario?.nombre ?: ""
            val idActual = usuario?.id ?: -1L
            turnos.filter {
                it.usuarioId == idActual || (nombreActual.isNotEmpty() && it.observadorNombre.equals(nombreActual, ignoreCase = true))
            }
        }
    }

    val fechaHoy = remember { Fechas.hoy() }
    val horaActual = remember { Fechas.horaActual() }

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
                    items(turnosFiltrados, key = { it.id }) { turno ->
                        val estado = calcularEstado(turno)
                        val colorEstado = when (estado) {
                            Sesion.PROGRAMADA -> MaterialTheme.colorScheme.tertiary
                            Sesion.EN_CURSO -> MaterialTheme.colorScheme.primary
                            Sesion.REALIZADA -> MaterialTheme.colorScheme.outline
                        }
                        val accesosNombres = remember { CatalogosRincon.accesosInfo.toMap() }
                        val nombrePuerta = accesosNombres[turno.acceso] ?: "Norte"

                        TarjetaSeccion(
                            titulo = "Acceso $nombrePuerta • ${turno.observadorNombre}",
                            modifier = Modifier
                                .fillMaxWidth()
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
                                        text = "💡 Toca la tarjeta para ver registros",
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
                                            text = estado.etiqueta,
                                            color = colorEstado,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    if (esAdmin && modoAdmin) {
                                        IconButton(onClick = { onEditarTurno(turno.id) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_editar),
                                                contentDescription = "Editar Turno",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { turnoParaEliminar = turno }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_eliminar),
                                                contentDescription = "Eliminar Turno",
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

    // Diálogo de Confirmación para Eliminar Turno
    turnoParaEliminar?.let { turno ->
        AlertDialog(
            onDismissRequest = { turnoParaEliminar = null },
            title = { Text("Eliminar Turno") },
            text = { Text("¿Estás seguro de que deseas eliminar el turno asignado a ${turno.observadorNombre} (${turno.fecha} - ${turno.horaInicio} a ${turno.horaFin})?") },
            confirmButton = {
                TextButton(onClick = {
                    usuariosViewModel?.eliminarTurno(turno.id)
                    onRecargarTurnos()
                    turnoParaEliminar = null
                    Toast.makeText(context, "Turno eliminado con éxito", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { turnoParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
