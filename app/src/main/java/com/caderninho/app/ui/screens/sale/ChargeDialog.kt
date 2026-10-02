package com.caderninho.app.ui.screens.sale

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.caderninho.app.util.Formatters
import java.time.LocalDate

@Composable
internal fun ChargeDialog(
    state: ChargeUiState,
    onSetSale: (Long, Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Selecionar vendas para cobrar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ChargeSelectionActions(state, onSelectAll)
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.sales, key = { it.sale.sale.id }) { item ->
                        ChargeSaleRow(item, onSetSale)
                    }
                }
                ChargeTotal(state.selectedTotalCents)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.selectedTotalCents > 0
            ) { Text("Abrir WhatsApp") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
private fun ChargeSelectionActions(
    state: ChargeUiState,
    onSelectAll: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = state.allSelected,
                role = Role.Checkbox,
                onValueChange = onSelectAll
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(state.allSelected, onCheckedChange = null)
        Text("Selecionar todas", modifier = Modifier.weight(1f))
        TextButton(
            enabled = state.selectedTotalCents > 0,
            onClick = { onSelectAll(false) }
        ) {
            Text("Limpar")
        }
    }
}

@Composable
private fun ChargeSaleRow(
    item: ChargeSaleUiModel,
    onSetSale: (Long, Boolean) -> Unit
) {
    val containerColor = if (item.isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = SELECTED_ALPHA)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = CONTAINER_ALPHA)
    }
    val borderColor = if (item.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = item.isSelected,
                    role = Role.Checkbox,
                    onValueChange = { isSelected ->
                        onSetSale(item.sale.sale.id, isSelected)
                    }
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = null
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    Formatters.currencyFromCents(item.sale.totalCents),
                    style = MaterialTheme.typography.titleMedium
                )
                item.sale.sale.dueEpochDay?.let {
                    Text(
                        "Previsto: ${LocalDate.ofEpochDay(it).formatDate()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ChargeTotal(selectedTotalCents: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "Total selecionado: ${Formatters.currencyFromCents(selectedTotalCents)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private const val CONTAINER_ALPHA = 0.72f
private const val SELECTED_ALPHA = 0.88f
