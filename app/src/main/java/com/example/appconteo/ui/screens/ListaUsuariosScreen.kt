package com.example.appconteo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.appconteo.R
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
    onEliminarUsuario: (Usuario) -> Unit,
    onEditar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    var porEliminar by remember { mutableStateOf<Usuario?>(null) }
    var modoEliminar by remember { mutableStateOf(false) }

    val observadores = remember(usuarios) {
        usuarios.filter { it.rolId != 1L }
    }

    porEliminar?.let { usuario ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text(stringResource(R.string.dialogo_eliminar_titulo)) },
            text = { Text(stringResource(R.string.dialogo_eliminar_mensaje, usuario.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminarUsuario(usuario)
                    porEliminar = null
                }) { Text(stringResource(R.string.btn_eliminar), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) {
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
                    IconButton(onClick = { modoEliminar = !modoEliminar }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eliminar),
                            contentDescription = "Activar modo eliminar",
                            tint = if (modoEliminar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
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
                    Card(onClick = { onEditar(usuario.id) }) {
                        ListItem(
                            overlineContent = { Text(usuario.rolNombre.ifEmpty { "Observador" }) },
                            leadingContent = {
                                Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                            },
                            headlineContent = { Text(usuario.nombre) },
                            supportingContent = { Text(usuario.correo) },
                            trailingContent = {
                                if (modoEliminar && usuario.id != usuarioActual?.id) {
                                    IconButton(onClick = { porEliminar = usuario }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_eliminar),
                                            contentDescription = stringResource(R.string.cd_eliminar),
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
    Usuario(nombre = "Diego Muñoz", correo = "diego@duocuc.cl", id = 3, rolId = 3, rolNombre = "Observador")
)

private val adminPreviewUser = Usuario(nombre = "Admin Principal", correo = "admin@demo.cl", id = 1, rolId = 1, rolNombre = "Administrador")

@Preview(showBackground = true, showSystemUi = true, name = "Admin - Con Usuarios")
@Composable
private fun AdminPreview() {
    AppConteoTheme {
        UsuariosScreen(
            usuarios = listaUsuariosPreview,
            usuarioActual = adminPreviewUser,
            filtro = "",
            onBuscar = {},
            onEliminarUsuario = {},
            onVolver = {},
            onEditar = {}
        )
    }
}