package com.caderninho.app.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.ui.components.NotebookFilterChip
import com.caderninho.app.util.Formatters
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SummaryScreen(viewModel: SummaryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Resumo") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PeriodSelector(
                selected = state.period,
                onSelected = viewModel::selectPeriod
            )
            PeriodNavigation(
                state = state,
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
                onToday = viewModel::returnToToday
            )

            val periodLabel = if (state.period == SummaryPeriod.DAY) "dia" else "mês"
            SummaryCard(
                title = "Recebido no $periodLabel",
                value = Formatters.currencyFromCents(state.totalReceivedCents)
            )
            SummaryCard(
                title = "Pendente (fiado)",
                value = Formatters.currencyFromCents(state.totalPendingCents)
            )
            SummaryCard(
                title = "Vendas/serviços considerados",
                value = state.saleCount.toString()
            )

            if (state.byPaymentMethodCents.isNotEmpty()) {
                Text("Recebido por forma de pagamento", style = MaterialTheme.typography.titleMedium)
                state.byPaymentMethodCents.forEach { (method, valueCents) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(method.label())
                        Text(Formatters.currencyFromCents(valueCents))
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selected: SummaryPeriod,
    onSelected: (SummaryPeriod) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryPeriod.entries.forEach { period ->
            NotebookFilterChip(
                selected = selected == period,
                onClick = { onSelected(period) },
                label = if (period == SummaryPeriod.DAY) "Dia" else "Mês"
            )
        }
    }
}

@Composable
private fun PeriodNavigation(
    state: SummaryUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Período anterior"
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.periodLabel(), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onToday) { Text("Hoje") }
        }
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Próximo período"
            )
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

private fun SummaryUiState.periodLabel(): String {
    val pattern = when (period) {
        SummaryPeriod.DAY -> "dd 'de' MMMM 'de' yyyy"
        SummaryPeriod.MONTH -> "MMMM 'de' yyyy"
    }
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    return selectedDate.format(DateTimeFormatter.ofPattern(pattern, locale))
        .replaceFirstChar { it.titlecase(locale) }
}

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Dinheiro"
    PaymentMethod.PIX -> "Pix"
    PaymentMethod.CREDIT_CARD -> "Cartão de crédito"
    PaymentMethod.DEBIT_CARD -> "Cartão de débito"
    PaymentMethod.OTHER -> "Outro"
}
