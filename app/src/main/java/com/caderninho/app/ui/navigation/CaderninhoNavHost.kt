package com.caderninho.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.caderninho.app.ui.screens.clientes.ClientesScreen
import com.caderninho.app.ui.screens.resumo.ResumoScreen
import com.caderninho.app.ui.screens.venda.ClienteDetalheScreen

/** Grafo de navegação principal do app: abas inferiores + tela de detalhe do cliente. */
@Composable
fun CaderninhoNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val destinoAtual = backStackEntry?.destination
            val mostrarBarra = Destino.abas.any { destinoAtual?.hierarchy?.any { h -> h.route == it.rota } == true }
            if (mostrarBarra) {
                NavigationBar {
                    Destino.abas.forEach { destino ->
                        val selecionado = destinoAtual?.hierarchy?.any { it.route == destino.rota } == true
                        NavigationBarItem(
                            selected = selecionado,
                            onClick = {
                                navController.navigate(destino.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destino.icone, contentDescription = destino.titulo) },
                            label = { Text(destino.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Clientes.rota,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destino.Clientes.rota) {
                ClientesScreen(onClienteClick = { clienteId ->
                    navController.navigate(rotaDetalheCliente(clienteId))
                })
            }
            composable(Destino.Resumo.rota) {
                ResumoScreen()
            }
            composable(
                route = ROTA_DETALHE_CLIENTE,
                arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
            ) {
                ClienteDetalheScreen()
            }
        }
    }
}
