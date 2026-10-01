package com.caderninho.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

/** Destinos de navegação de nível superior (abas do app). */
sealed class Destino(val rota: String, val titulo: String, val icone: ImageVector) {
    data object Clientes : Destino("clientes", "Clientes", Icons.Filled.People)
    data object Resumo : Destino("resumo", "Resumo", Icons.Filled.BarChart)

    companion object {
        val abas = listOf(Clientes, Resumo)
    }
}

const val ROTA_DETALHE_CLIENTE = "cliente/{clienteId}?cobrar={cobrar}"

fun rotaDetalheCliente(clienteId: Long, cobrar: Boolean = false) =
    "cliente/$clienteId?cobrar=$cobrar"
