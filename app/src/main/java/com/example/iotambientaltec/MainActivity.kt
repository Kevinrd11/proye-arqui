package com.example.iotambientaltec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.iotambientaltec.ui.navigation.AppNavigation
import com.example.iotambientaltec.ui.screens.AppViewModelFactory
import com.example.iotambientaltec.ui.theme.IoTAmbientalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IoTAmbientalTheme { AppNavigation(factory = AppViewModelFactory()) }
        }
    }
}
