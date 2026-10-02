package com.caderninho.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

/** Destinos de navegação de nível superior (abas do app). */
sealed class Destination(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Destination("home", "Início", Icons.Filled.Home)
    data object Clients : Destination("clients", "Clientes", Icons.Filled.People)
    data object Summary : Destination("summary", "Resumo", Icons.Filled.BarChart)

    companion object {
        val tabs = listOf(Home, Clients, Summary)
    }
}

const val CLIENT_DETAIL_ROUTE = "client/{clientId}?charge={charge}"

fun clientDetailRoute(clientId: Long, charge: Boolean = false) =
    "client/$clientId?charge=$charge"
