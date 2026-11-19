package com.example.appconteo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.components.AvatarCircular
import coil3.compose.AsyncImage
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

// CAMBIO: antes recibía un Bitmap decodificado del BLOB de SQLite.
// Ahora recibe la URL de la foto en el servidor y Coil (AsyncImage) la descarga y la muestra.
@Composable
fun FotoPerfil(
    fotoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(Dimens.tamanoAvatar)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AvatarCircular(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FotoPerfilPreview() {
    AppConteoTheme {
        FotoPerfil(
            fotoUrl = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}
