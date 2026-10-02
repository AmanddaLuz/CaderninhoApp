package com.caderninho.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.caderninho.app.ui.screens.clients.ClientsScreen
import com.caderninho.app.ui.screens.home.HomeScreen
import com.caderninho.app.ui.screens.summary.SummaryScreen
import com.caderninho.app.ui.screens.sale.ClientDetailScreen

/** Grafo de navegação principal do app: abas inferiores + tela de detalhe do cliente. */
@Composable
fun LedgerNavHost(
    clientToCharge: Long? = null,
    onChargeConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(clientToCharge) {
        clientToCharge?.let { clientId ->
            navController.navigate(clientDetailRoute(clientId, charge = true)) {
                launchSingleTop = true
            }
            onChargeConsumed()
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = { LedgerBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onOpenClients = { navController.openTopLevel(Destination.Clients) },
                    onOpenSummary = { navController.openTopLevel(Destination.Summary) },
                    onOpenPrivacyPolicy = { uriHandler.openUri(PRIVACY_POLICY_URL) }
                )
            }
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
                ClientDetailScreen(onBack = navController::popBackStack)
            }
        }
    }
}

@Composable
private fun LedgerBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBar = Destination.tabs.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }
    if (!showBar) return

    NavigationBar {
        Destination.tabs.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.openTopLevel(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.title) },
                label = { Text(destination.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

private fun NavHostController.openTopLevel(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private const val PRIVACY_POLICY_URL =
    "https://github.com/AmanddaLuz/CaderninhoApp/blob/main/docs/legal/privacy-policy.md"
