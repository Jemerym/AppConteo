package com.example.appconteo.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.components.AvatarCircular
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@Composable
fun FotoPerfil(
    fotoBitmap: Bitmap? = null,
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
        if (fotoBitmap != null) {
            Image(
                bitmap = fotoBitmap.asImageBitmap(),
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
            fotoBitmap = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}