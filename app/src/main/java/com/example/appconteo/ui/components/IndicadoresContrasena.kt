package com.example.appconteo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appconteo.model.Validaciones
import com.example.appconteo.ui.theme.Dimens

@Composable
fun IndicadoresContrasena(contrasena: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.espacioMini),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "Requisitos de contraseña:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )

        Validaciones.criteriosContrasena.forEach { criterio ->
            FilaCriterio(cumple = criterio.cumple(contrasena), texto = criterio.texto)
        }
    }
}

@Composable
private fun FilaCriterio(cumple: Boolean, texto: String) {
    val color = if (cumple) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icono = if (cumple) "✓ " else "✗ "

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icono, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text(text = texto, color = color, style = MaterialTheme.typography.labelSmall)
    }
}