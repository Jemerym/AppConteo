package com.example.appconteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.appconteo.ui.theme.AppConteoTheme
import com.example.appconteo.navigation.AppNavegacion
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppConteoTheme {

                AppNavegacion()
            }
        }
    }
}