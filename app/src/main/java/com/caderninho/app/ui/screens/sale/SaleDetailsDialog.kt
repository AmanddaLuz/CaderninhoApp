package com.caderninho.app.ui.screens.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.util.Formatters

@Composable
internal fun SaleDetailsDialog(
    sale: SaleWithItems,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Itens da venda") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sale.items.sortedBy { it.position }, key = { it.id }) { item ->
                    SaleItemDetail(item)
                }
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            Formatters.currencyFromCents(sale.totalCents),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text("Fechar") }
        }
    )
}

@Composable
private fun SaleItemDetail(item: SaleItemEntity) {
    Column {
        Text(item.description, style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${item.quantity.formatQuantity()} × " +
                    Formatters.currencyFromCents(item.unitValueCents)
            )
            Text(Formatters.currencyFromCents(item.subtotalCents))
        }
    }
}

private fun Double.formatQuantity(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString().replace('.', ',')
