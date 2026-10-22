package com.example.appconteo.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.appconteo.viewmodel.UsuariosViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.data.ConvertidorImagen
import com.example.appconteo.model.Usuario
import com.example.appconteo.permisos.Permisos
import com.example.appconteo.ui.components.BotonPrincipal
import com.example.appconteo.ui.components.BotonSecundario
import com.example.appconteo.ui.components.TarjetaSeccion
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onFotoTomada: (Bitmap) -> Unit,
    onImagenElegida: (Uri) -> Unit,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fotoBitmap = remember(usuario?.avatar) {
        ConvertidorImagen.aBitmap(usuario?.avatar)
    }

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val res = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                Permisos.abrirAjustes(context)
            }
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) onFotoTomada(foto)
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No se encontró app de cámara") }
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else avisarPermisoDenegado("Se requiere permiso de cámara")
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) {
            onImagenElegida(uri)
        }
    }

    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            lanzadorGaleria.launch(intent)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No se pudo abrir la galería") }
        }
    }

    val pedirPermisoGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado("Se requiere permiso para acceder a imágenes")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (fotoBitmap != null) {
                    Image(
                        bitmap = fotoBitmap.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_persona),
                        contentDescription = "Avatar sin foto",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))

            Text(
                text = usuario?.nombre ?: "Usuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = Dimens.espacioChico)
            ) {
                Text(
                    text = usuario?.rolNombre?.ifBlank { "Estudiante" } ?: "Estudiante",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Text(
                text = usuario?.correo ?: "sin_correo@duocuc.cl",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.espacioChico)
            )

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            TarjetaSeccion(
                titulo = "Cambiar Fotografía",
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    BotonPrincipal(
                        texto = "Cámara",
                        onClick = {
                            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
                            else pedirPermisoCamara.launch(Permisos.CAMARA)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    BotonSecundario(
                        texto = "Galería",
                        onClick = {
                            if (Permisos.tiene(context, Permisos.IMAGEN)) abrirGaleria()
                            else pedirPermisoGaleria.launch(Permisos.IMAGEN)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.espacioMedio))

            TarjetaSeccion(
                titulo = "Detalles de la Cuenta",
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    FilaDetalle(etiqueta = "ID Usuario:", valor = "#${usuario?.id ?: 1}")
                    FilaDetalle(etiqueta = "Correo:", valor = usuario?.correo ?: "-")
                    FilaDetalle(etiqueta = "Rol:", valor = usuario?.rolNombre ?: "Estudiante")
                }
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))

            Button(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.espacioPantalla)
            ) {
                Text("Cerrar Sesión")
            }

            Spacer(modifier = Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


private val perfilPreview = Usuario(
    nombre = "Benjamin Curiñanco",
    correo = "be.curinanco@duocuc.cl",
    id = 1,
    rolNombre = "Administrador"


)

@Preview(showBackground = true, showSystemUi = true, name = "Modo Claro")
@Composable
private fun PerfilPreview() {
    AppConteoTheme {
        PerfilScreen(
            usuario = perfilPreview,
            onFotoTomada = {},
            onImagenElegida = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Modo Oscuro")
@Composable
private fun PerfilOscuroPreview() {
    AppConteoTheme(darkTheme = true) {
        PerfilScreen(
            usuario = perfilPreview,
            onFotoTomada = {},
            onImagenElegida = {},
            onCerrarSesion = {}
        )
    }

}