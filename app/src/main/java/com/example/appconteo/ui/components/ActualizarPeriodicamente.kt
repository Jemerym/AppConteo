package com.example.appconteo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/**
 * Ejecuta [accion] al entrar a la pantalla y luego cada [intervaloMs] milisegundos,
 * SOLO mientras la pantalla está visible y la app en primer plano
 * (se pausa sola al minimizar la app o al navegar a otra pantalla, y se reanuda al volver).
 *
 * Uso:  ActualizarPeriodicamente { turnosViewModel.refrescarEnSegundoPlano() }
 */
@Composable
fun ActualizarPeriodicamente(
    intervaloMs: Long = 15_000L,
    accion: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val accionActual by rememberUpdatedState(accion)

    LaunchedEffect(lifecycleOwner, intervaloMs) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                accionActual()
                delay(intervaloMs)
            }
        }
    }
}
