package com.caderninho.app.ui.screens.venda

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
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaItemEntity
import com.caderninho.app.util.Formatadores

@Composable
internal fun DetalhesVendaDialog(
    venda: VendaComItens,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text("Itens da venda") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(venda.itens.sortedBy { it.ordem }, key = { it.id }) { item ->
                    ItemVendaDetalhe(item)
                }
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            Formatadores.moedaCentavos(venda.totalCentavos),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Fechar") }
        }
    )
}

@Composable
private fun ItemVendaDetalhe(item: VendaItemEntity) {
    Column {
        Text(item.descricao, style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${item.quantidade.formatarQuantidade()} × " +
                    Formatadores.moedaCentavos(item.valorUnitarioCentavos)
            )
            Text(Formatadores.moedaCentavos(item.subtotalCentavos))
        }
    }
}

private fun Double.formatarQuantidade(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString().replace('.', ',')
