package com.caderninho.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.util.Formatters

@Composable
fun HomeScreen(
    onOpenClients: () -> Unit,
    onOpenSummary: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Caderninho", style = MaterialTheme.typography.headlineMedium)
            PrivacySummaryCard(state, viewModel::toggleValuesVisibility)
            IndicatorsRow(state)
            Text("Acesso rápido", style = MaterialTheme.typography.titleMedium)
            QuickActionCard(
                title = "Clientes e vendas",
                subtitle = "Consulte clientes ou escolha um para registrar uma venda",
                icon = Icons.Filled.Groups,
                onClick = onOpenClients
            )
            QuickActionCard(
                title = "Cobranças",
                subtitle = "${state.overdueSaleCount} atrasadas e ${state.dueTodayCount} para hoje",
                icon = Icons.Filled.NotificationsActive,
                onClick = onOpenClients
            )
            QuickActionCard(
                title = "Resumo",
                subtitle = "Acompanhe o dia ou o mês sem expor clientes",
                icon = Icons.Filled.Assessment,
                onClick = onOpenSummary
            )
        }
    }
}

@Composable
private fun PrivacySummaryCard(
    state: HomeUiState,
    onToggleVisibility: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = FINANCIAL_CARD_ALPHA
            ),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = BORDER_ALPHA)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visão financeira", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (state.valuesVisible) {
                            Icons.Filled.VisibilityOff
                        } else {
                            Icons.Filled.Visibility
                        },
                        contentDescription = if (state.valuesVisible) {
                            "Ocultar valores"
                        } else {
                            "Mostrar valores"
                        }
                    )
                }
            }
            FinancialLine(
                label = "Total pendente",
                value = state.pendingTotalCents.protectedCurrency(state.valuesVisible)
            )
            FinancialLine(
                label = "Previsto para hoje",
                value = state.dueTodayTotalCents.protectedCurrency(state.valuesVisible)
            )
        }
    }
}

@Composable
private fun FinancialLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun IndicatorsRow(state: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IndicatorCard("Clientes", state.clientCount, Modifier.weight(1f))
        IndicatorCard("Pendências", state.pendingSaleCount, Modifier.weight(1f))
        IndicatorCard("Atrasadas", state.overdueSaleCount, Modifier.weight(1f))
    }
}

@Composable
private fun IndicatorCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_ALPHA)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_ALPHA)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun Long.protectedCurrency(isVisible: Boolean): String =
    if (isVisible) Formatters.currencyFromCents(this) else "R$ ••••••"

private const val CARD_ALPHA = 0.94f
private const val FINANCIAL_CARD_ALPHA = 0.88f
private const val BORDER_ALPHA = 0.55f
