package com.example.appconteo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appconteo.ui.style.EstilosTarjeta
import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.ui.theme.Dimens

@Composable
fun TarjetaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    colorValor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier,
        shape = EstilosTarjeta.forma,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = titulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorValor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaMetricaPreview() {
    AppConteoTheme() {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaMetrica(
                titulo = "Gente en tienda",
                valor = "100",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
