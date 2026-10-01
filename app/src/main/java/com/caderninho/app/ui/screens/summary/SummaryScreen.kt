package com.caderninho.app.ui.screens.summary

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
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.util.Formatters

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SummaryScreen(viewModel: SummaryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

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
            SummaryCard(title = "Recebido no mês", value = Formatters.currency(state.totalReceived))
            SummaryCard(title = "Pendente (fiado)", value = Formatters.currency(state.totalPending))
            SummaryCard(title = "Vendas/serviços registrados", value = state.saleCount.toString())

            if (state.byPaymentMethod.isNotEmpty()) {
                Text("Recebido por forma de pagamento", style = MaterialTheme.typography.titleMedium)
                state.byPaymentMethod.forEach { (method, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(method.label())
                        Text(Formatters.currency(value))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Dinheiro"
    PaymentMethod.PIX -> "Pix"
    PaymentMethod.CREDIT_CARD -> "Cartão de crédito"
    PaymentMethod.DEBIT_CARD -> "Cartão de débito"
    PaymentMethod.OTHER -> "Outro"
}
