package com.caderninho.app.ui.screens.resumo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.util.Formatadores

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ResumoScreen(viewModel: ResumoViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Resumo do mês") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResumoCard(titulo = "Recebido no mês", valor = Formatadores.moeda(estado.totalRecebido))
            ResumoCard(titulo = "Pendente (fiado)", valor = Formatadores.moeda(estado.totalPendente))
            ResumoCard(titulo = "Vendas/serviços registrados", valor = estado.quantidadeVendas.toString())

            if (estado.porFormaPagamento.isNotEmpty()) {
                Text("Recebido por forma de pagamento", style = MaterialTheme.typography.titleMedium)
                estado.porFormaPagamento.forEach { (forma, valor) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(forma.rotulo())
                        Text(Formatadores.moeda(valor))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumoCard(titulo: String, valor: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            Text(valor, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun FormaPagamento.rotulo(): String = when (this) {
    FormaPagamento.DINHEIRO -> "Dinheiro"
    FormaPagamento.PIX -> "Pix"
    FormaPagamento.CARTAO_CREDITO -> "Cartão de crédito"
    FormaPagamento.CARTAO_DEBITO -> "Cartão de débito"
    FormaPagamento.OUTRO -> "Outro"
}
