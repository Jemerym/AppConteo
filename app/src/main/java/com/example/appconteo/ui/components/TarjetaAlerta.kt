package com.example.appconteo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@Composable
fun TarjetaAlerta(
    mensaje: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(Dimens.radioCampo),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(Dimens.espacioMedio)
        )
    }
}
@Preview(showBackground = true)
@Composable
private fun TarjetaAlertaPreview() {
    AppConteoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TarjetaAlerta(mensaje = "9 personas en la tienda")
        }
    }
}