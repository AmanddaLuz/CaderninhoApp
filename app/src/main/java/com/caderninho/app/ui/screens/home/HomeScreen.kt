package com.caderninho.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.util.Formatters

@Composable
fun HomeScreen(
    onOpenClients: () -> Unit,
    onOpenPendingClients: () -> Unit,
    onOpenOverdueClients: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
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
            IndicatorsRow(
                state = state,
                onOpenClients = onOpenClients,
                onOpenPendingClients = onOpenPendingClients,
                onOpenOverdueClients = onOpenOverdueClients
            )
            TipsCard()
            TextButton(
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Política de privacidade")
            }
        }
    }
}

private data class Tip(val title: String, val description: String)

private val TIPS = listOf(
    Tip(
        title = "Veja quem está devendo",
        description = "Toque nos cards \"Pendências\" ou \"Atrasadas\" para ir direto aos clientes com saldo em aberto."
    ),
    Tip(
        title = "Cobre pelo WhatsApp",
        description = "No detalhe do cliente, toque em \"Cobrar\", selecione as vendas e envie a cobrança em um toque."
    ),
    Tip(
        title = "Proteja sua privacidade",
        description = "Toque no ícone de olho na Visão financeira para ocultar os valores antes de mostrar a tela."
    ),
    Tip(
        title = "Encontre um cliente rápido",
        description = "Use a busca por nome ou telefone na tela de Clientes para localizar alguém na hora."
    ),
    Tip(
        title = "Edite quando precisar",
        description = "Toque no lápis em qualquer cliente da lista para corrigir nome, telefone ou CPF."
    )
)

@Composable
private fun TipsCard() {
    val pagerState = rememberPagerState(pageCount = { TIPS.size })
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = CARD_ALPHA)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Dicas de uso", style = MaterialTheme.typography.titleMedium)
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().heightIn(min = TIP_MIN_HEIGHT)
            ) { page ->
                val tip = TIPS[page]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        tip.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tip.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
            TipsPageIndicator(pagerState)
        }
    }
}

@Composable
private fun TipsPageIndicator(pagerState: PagerState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { index ->
            val selected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (selected) SELECTED_DOT_SIZE else DOT_SIZE)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
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
private fun IndicatorsRow(
    state: HomeUiState,
    onOpenClients: () -> Unit,
    onOpenPendingClients: () -> Unit,
    onOpenOverdueClients: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IndicatorCard("Clientes", state.clientCount, onOpenClients, Modifier.weight(1f))
        IndicatorCard("Pendências", state.pendingSaleCount, onOpenPendingClients, Modifier.weight(1f))
        IndicatorCard("Atrasadas", state.overdueSaleCount, onOpenOverdueClients, Modifier.weight(1f))
    }
}

@Composable
private fun IndicatorCard(
    label: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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

private fun Long.protectedCurrency(isVisible: Boolean): String =
    if (isVisible) Formatters.currencyFromCents(this) else "R$ ••••••"

private const val CARD_ALPHA = 0.94f
private const val FINANCIAL_CARD_ALPHA = 0.88f
private const val BORDER_ALPHA = 0.55f
private val TIP_MIN_HEIGHT = 72.dp
private val DOT_SIZE = 6.dp
private val SELECTED_DOT_SIZE = 8.dp
