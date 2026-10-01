package com.caderninho.app.ui.screens.sale

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
import androidx.compose.material3.MaterialTheme
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
            Column {
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
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(state.sales, key = { it.sale.sale.id }) { item ->
                        ChargeSaleRow(item, onSetSale)
                    }
                }
                Text(
                    "Total selecionado: " +
                        Formatters.currencyFromCents(state.selectedTotalCents),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
private fun ChargeSaleRow(
    item: ChargeSaleUiModel,
    onSetSale: (Long, Boolean) -> Unit
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
            .padding(vertical = 4.dp),
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
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
