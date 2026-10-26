package cl.duocuc.miregistro.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.R
import com.example.appconteo.ui.theme.Dimens
import com.example.appconteo.ui.theme.AppConteoTheme

@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int = R.drawable.ic_persona,
    descripcion: String = "Avatar de usuario sin foto",
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
        Icon(
            painter = painterResource(id = imagen),
            contentDescription = descripcion,
            modifier = Modifier.size(80.dp),

        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    AppConteoTheme() {
        AvatarCircular(imagen = R.drawable.ic_persona, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}
