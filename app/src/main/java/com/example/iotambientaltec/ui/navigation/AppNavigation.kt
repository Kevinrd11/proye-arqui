package com.example.iotambientaltec.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.iotambientaltec.ui.screens.*

object Routes { const val Home = "home"; const val Dashboard = "dashboard"; const val Queries = "queries"; const val Charts = "charts"; const val Map = "map" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(factory: AppViewModelFactory, navController: NavHostController = rememberNavController()) {
    val current = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(topBar = {
        TopAppBar(title = { Text("IoT Ambiental TEC") }, navigationIcon = { if (current != Routes.Home) IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Volver") } })
    }) { padding ->
        NavHost(navController, startDestination = Routes.Home, modifier = Modifier.padding(padding)) {
            composable(Routes.Home) { HomeScreen({ navController.navigate(Routes.Dashboard) }, { navController.navigate(Routes.Queries) }, { navController.navigate(Routes.Charts) }, { navController.navigate(Routes.Map) }) }
            composable(Routes.Dashboard) { DashboardScreen(factory) }
            composable(Routes.Queries) { QueryScreen(factory) }
            composable(Routes.Charts) { ChartsScreen(factory) }
            composable(Routes.Map) { MapScreen(factory) }
        }
    }
}
