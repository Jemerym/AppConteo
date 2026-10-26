package com.example.appconteo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.appconteo.R
import com.example.appconteo.ui.theme.AppConteoTheme

@Composable
fun BotonVolver(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    descripcion: String = "Avatar de usuario sin foto",
    @DrawableRes icono: Int = R.drawable.ic_volver,
) {
    IconButton(
        onClick = onVolver,
        modifier = modifier,
        enabled = habilitado
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = descripcion,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonVolverPreview() {
    AppConteoTheme {
        BotonVolver(onVolver = {})
    }
}