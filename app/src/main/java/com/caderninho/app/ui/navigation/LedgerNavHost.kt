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
                    onOpenClients = { navController.openClients() },
                    onOpenPendingClients = { navController.openClients(ClientsFilter.PENDING) },
                    onOpenOverdueClients = { navController.openClients(ClientsFilter.OVERDUE) },
                    onOpenPrivacyPolicy = { uriHandler.openUri(PRIVACY_POLICY_URL) }
                )
            }
            composable(
                route = Destination.Clients.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = ClientsFilter.ALL.name
                    }
                )
            ) {
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
                onClick = {
                    if (destination == Destination.Clients) {
                        navController.openClients()
                    } else {
                        navController.openTopLevel(destination)
                    }
                },
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

// Nenhuma das abas usa saveState/restoreState: misturar esse mecanismo (usado
// no padrão clássico de bottom nav) com a rota de Clientes, que tem argumento
// dinâmico (filtro), deixava o NavController com estado salvo inconsistente e
// o botão Início parava de voltar para a Home depois de visitar outra aba.
private fun NavHostController.openTopLevel(destination: Destination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}

private fun NavHostController.openClients(filter: ClientsFilter = ClientsFilter.ALL) {
    navigate(clientsRoute(filter)) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}

private const val PRIVACY_POLICY_URL =
    "https://amanddaluz.github.io/caderninho-privacy/"
