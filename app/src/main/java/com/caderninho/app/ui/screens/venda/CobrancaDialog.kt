package com.caderninho.app.ui.screens.venda

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caderninho.app.util.Formatadores
import java.time.LocalDate

@Composable
internal fun CobrancaDialog(
    estado: CobrancaUiState,
    onAlternarVenda: (Long) -> Unit,
    onSelecionarTodas: (Boolean) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Selecionar vendas para cobrar") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(estado.todasSelecionadas, onSelecionarTodas)
                    Text("Selecionar todas")
                }
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(estado.vendas, key = { it.venda.venda.id }) { item ->
                        VendaCobrancaLinha(item, onAlternarVenda)
                    }
                }
                Text(
                    "Total selecionado: " +
                        Formatadores.moedaCentavos(estado.totalSelecionadoCentavos),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                enabled = estado.totalSelecionadoCentavos > 0
            ) { Text("Abrir WhatsApp") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun VendaCobrancaLinha(
    item: VendaCobrancaUiModel,
    onAlternarVenda: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.selecionada,
            onCheckedChange = { onAlternarVenda(item.venda.venda.id) }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.venda.itens.joinToString { it.descricao })
            Text(
                Formatadores.moedaCentavos(item.venda.totalCentavos),
                style = MaterialTheme.typography.labelMedium
            )
            item.venda.venda.vencimentoEpochDay?.let {
                Text(
                    "Previsto: ${LocalDate.ofEpochDay(it).formatarData()}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
