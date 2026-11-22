package com.example.appconteo.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.appconteo.R
import com.example.appconteo.model.Rol
import com.example.appconteo.model.Usuario
import com.example.appconteo.ui.components.CampoTexto
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    usuarios: List<Usuario>,
    usuarioActual: Usuario?,
    filtro: String,
    onBuscar: (String) -> Unit,
    onAnularUsuario: (Usuario) -> Unit,
    onRestaurarUsuario: (Usuario) -> Unit = {},
    onVerRegistros: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // CAMBIO: ya no se elimina. Se ANULA (no puede entrar, pero se conserva su registro) y se puede RESTAURAR.
    var porAnular by remember { mutableStateOf<Usuario?>(null) }
    var porRestaurar by remember { mutableStateOf<Usuario?>(null) }
    var modoGestion by remember { mutableStateOf(false) }
    var mostrarAnulados by rememberSaveable { mutableStateOf(false) }

    // El botón "atrás" del teléfono también pasa por onVolver (así también se limpia el buscador)
    BackHandler(onBack = onVolver)

    val observadores = remember(usuarios, mostrarAnulados) {
        usuarios.filter { !it.esAdmin && (mostrarAnulados || it.activo) }
    }
    val cantidadAnulados = remember(usuarios) { usuarios.count { !it.esAdmin && !it.activo } }

    porAnular?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porAnular = null },
            title = { Text("Anular usuario") },
            text = {
                Text(
                    "¿Anular a ${usuario.nombre}? No podrá iniciar sesión y sus turnos quedarán anulados. " +
                            "Sus registros se conservan y puedes restaurarlo cuando quieras."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAnularUsuario(usuario)
                    porAnular = null
                }) { Text("Anular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { porAnular = null }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    porRestaurar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porRestaurar = null },
            title = { Text("Restaurar usuario") },
            text = { Text("¿Restaurar a ${usuario.nombre}? Podrá volver a iniciar sesión. Sus turnos anulados no se restauran solos.") },
            confirmButton = {
                TextButton(onClick = {
                    onRestaurarUsuario(usuario)
                    porRestaurar = null
                }) { Text("Restaurar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { porRestaurar = null }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_usuarios)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { modoGestion = !modoGestion }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eliminar),
                            contentDescription = "Activar modo anular",
                            tint = if (modoGestion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CampoTexto(
                valor = filtro,
                onValorCambia = onBuscar,
                etiqueta = stringResource(R.string.campo_buscar),
                icono = R.drawable.ic_buscar,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
            )

            FilterChip(
                selected = mostrarAnulados,
                onClick = { mostrarAnulados = !mostrarAnulados },
                label = { Text("Mostrar anulados ($cantidadAnulados)") },
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio)
            )

            if (observadores.isEmpty()) {
                Text(
                    text = stringResource(R.string.lista_vacia),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(observadores, key = { it.id }) { usuario ->
                    Card(
                        onClick = { onVerRegistros(usuario.id) },
                        modifier = Modifier.alpha(if (usuario.activo) 1f else 0.55f)
                    ) {
                        ListItem(
                            overlineContent = {
                                Text(
                                    if (usuario.activo) usuario.rolNombre.ifEmpty { "Observador" }
                                    else "ANULADO",
                                    color = if (usuario.activo) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.error
                                )
                            },
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                            },
                            headlineContent = { Text(usuario.nombre) },
                            supportingContent = { Text(usuario.correo) },
                            trailingContent = {
                                when {
                                    // Restaurar siempre está a la vista para los anulados
                                    !usuario.activo -> TextButton(onClick = { porRestaurar = usuario }) {
                                        Text("Restaurar")
                                    }
                                    modoGestion && usuario.id != usuarioActual?.id ->
                                        IconButton(onClick = { porAnular = usuario }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_eliminar),
                                                contentDescription = "Anular usuario",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
private val listaUsuariosPreview = listOf(
    Usuario(nombre = "Administrador Demo", correo = "admin@demo.cl", id = 1, rolId = 1, rolNombre = "Administrador"),
    Usuario(nombre = "Camila Rojas", correo = "camila@duocuc.cl", id = 2, rolId = 3, rolNombre = "Observador"),
    Usuario(nombre = "Diego Muñoz", correo = "diego@duocuc.cl", id = 3, rolId = 3, rolNombre = "Observador", activo = false)
)

private val adminPreviewUser = Usuario(nombre = "Admin Principal", correo = "a  dmin@demo.cl", id = 1, rolId = 1, rolNombre = "Administrador")

@Preview(showBackground = true, showSystemUi = true, name = "Admin - Con Usuarios")
@Composable
private fun AdminPreview() {
    AppConteoTheme {
        UsuariosScreen(
            usuarios = listaUsuariosPreview,
            usuarioActual = adminPreviewUser,
            filtro = "",
            onBuscar = {},
            onAnularUsuario = {},
            onVolver = {},
            onVerRegistros = {}
        )
    }
}
