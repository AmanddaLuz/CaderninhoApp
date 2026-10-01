package com.caderninho.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.caderninho.app.ui.screens.clients.ClientsScreen
import com.caderninho.app.ui.screens.summary.SummaryScreen
import com.caderninho.app.ui.screens.sale.ClientDetailScreen

/** Grafo de navegação principal do app: abas inferiores + tela de detalhe do cliente. */
@Composable
fun LedgerNavHost(
    clientToCharge: Long? = null,
    onChargeConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(clientToCharge) {
        clientToCharge?.let { clientId ->
            navController.navigate(clientDetailRoute(clientId, charge = true)) {
                launchSingleTop = true
            }
            onChargeConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val showBar = Destination.tabs.any { destination ->
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            }
            if (showBar) {
                NavigationBar {
                    Destination.tabs.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Clients.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Clients.route) {
                ClientsScreen(onClientClick = { clientId ->
                    navController.navigate(clientDetailRoute(clientId))
                })
            }
            composable(Destination.Summary.route) {
                SummaryScreen()
            }
            composable(
                route = CLIENT_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("clientId") { type = NavType.LongType },
                    navArgument("charge") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                ClientDetailScreen()
            }
        }
    }
}
