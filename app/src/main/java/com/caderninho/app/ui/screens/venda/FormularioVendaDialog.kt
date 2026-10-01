package com.caderninho.app.ui.screens.venda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.util.ValoresNumericos
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private data class LinhaItemUi(
    val id: Long,
    val descricao: String = "",
    val quantidade: String = "1",
    val valorUnitario: String = "R$ 0,00"
)

private class FormularioVendaEditor {
    val itens = mutableStateListOf<LinhaItemUi>()
    private var proximoId by mutableLongStateOf(1)
    var itemEmEdicao by mutableStateOf<LinhaItemUi?>(null)
    var forma by mutableStateOf(FormaPagamento.DINHEIRO)
    var jaPago by mutableStateOf(false)
    var vencimento by mutableStateOf<Long?>(null)
    var mostrarData by mutableStateOf(false)

    fun novoItem() {
        itemEmEdicao = LinhaItemUi(id = proximoId++)
    }

    fun salvarItem(item: LinhaItemUi) {
        val indice = itens.indexOfFirst { it.id == item.id }
        if (indice >= 0) itens[indice] = item else itens += item
        itemEmEdicao = null
    }

    fun removerItem(item: LinhaItemUi) {
        itens.remove(item)
    }

    fun itensFormulario(): List<ItemVendaFormulario> = itens.map {
        ItemVendaFormulario(it.descricao, it.quantidade, it.valorUnitario)
    }
}

@Composable
internal fun FormularioVendaDialog(
    mensagemErro: String?,
    onConfirmar: (List<ItemVendaFormulario>, FormaPagamento, Boolean, Long?) -> Unit,
    onCancelar: () -> Unit
) {
    val editor = remember { FormularioVendaEditor() }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova venda ou serviço") },
        text = { ConteudoFormularioVenda(editor, mensagemErro) },
        confirmButton = {
            TextButton(onClick = {
                onConfirmar(
                    editor.itensFormulario(),
                    editor.forma,
                    editor.jaPago,
                    editor.vencimento
                )
            }) { Text("Salvar venda") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
    DialogosFormularioVenda(editor)
}

@Composable
private fun ConteudoFormularioVenda(
    editor: FormularioVendaEditor,
    mensagemErro: String?
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (editor.itens.isEmpty()) {
            item {
                Text(
                    "Nenhum item adicionado.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        items(editor.itens, key = LinhaItemUi::id) { item ->
            ItemVendaResumo(
                item = item,
                onEditar = { editor.itemEmEdicao = item },
                onRemover = { editor.removerItem(item) }
            )
        }
        item {
            TextButton(onClick = editor::novoItem) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Adicionar item")
            }
        }
        item { SeletorFormaPagamento(editor) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Já foi pago agora?")
                Switch(
                    checked = editor.jaPago,
                    onCheckedChange = {
                        editor.jaPago = it
                        if (it) editor.vencimento = null
                    }
                )
            }
        }
        if (!editor.jaPago) {
            item {
                TextButton(onClick = { editor.mostrarData = true }) {
                    Text(
                        editor.vencimento?.let {
                            "Pagamento previsto: ${LocalDate.ofEpochDay(it).formatarData()}"
                        } ?: "Selecionar data prevista de pagamento"
                    )
                }
            }
        }
        mensagemErro?.let { erro ->
            item { Text(erro, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ItemVendaResumo(
    item: LinhaItemUi,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.descricao, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.quantidade} × ${item.valorUnitario}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onRemover) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover item")
            }
        }
    }
}

@Composable
private fun SeletorFormaPagamento(editor: FormularioVendaEditor) {
    var aberto by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { aberto = true }) {
            Text("Forma de pagamento: ${editor.forma.rotulo()}")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            FormaPagamento.entries.forEach { opcao ->
                DropdownMenuItem(text = { Text(opcao.rotulo()) }, onClick = {
                    editor.forma = opcao
                    aberto = false
                })
            }
        }
    }
}

@Composable
private fun DialogosFormularioVenda(editor: FormularioVendaEditor) {
    editor.itemEmEdicao?.let { item ->
        ItemVendaDialog(
            item = item,
            onSalvar = editor::salvarItem,
            onCancelar = { editor.itemEmEdicao = null }
        )
    }
    if (editor.mostrarData) {
        SeletorDataDialog(
            titulo = "Data prevista de pagamento",
            dataInicialEpochDay = editor.vencimento ?: LocalDate.now().toEpochDay(),
            onConfirmar = {
                editor.vencimento = it
                editor.mostrarData = false
            },
            onCancelar = { editor.mostrarData = false }
        )
    }
}

@Composable
private fun ItemVendaDialog(
    item: LinhaItemUi,
    onSalvar: (LinhaItemUi) -> Unit,
    onCancelar: () -> Unit
) {
    var descricao by remember(item.id) { mutableStateOf(item.descricao) }
    var quantidade by remember(item.id) { mutableStateOf(item.quantidade) }
    var valor by remember(item.id) {
        mutableStateOf(
            TextFieldValue(
                text = item.valorUnitario,
                selection = TextRange(item.valorUnitario.length)
            )
        )
    }
    val valido = descricao.isNotBlank() &&
        ValoresNumericos.quantidade(quantidade) != null &&
        ValoresNumericos.centavos(valor.text) != null

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (item.descricao.isBlank()) "Adicionar item" else "Editar item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        val formatado = ValoresNumericos.formatarMoedaDigitada(it.text)
                        valor = TextFieldValue(formatado, TextRange(formatado.length))
                    },
                    label = { Text("Valor unitário") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valido,
                onClick = {
                    onSalvar(
                        item.copy(
                            descricao = descricao.trim(),
                            quantidade = quantidade,
                            valorUnitario = valor.text
                        )
                    )
                }
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeletorDataDialog(
    titulo: String,
    dataInicialEpochDay: Long,
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit
) {
    val inicialMillis = LocalDate.ofEpochDay(dataInicialEpochDay)
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val estado = rememberDatePickerState(initialSelectedDateMillis = inicialMillis)
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { millis ->
                    onConfirmar(
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                    )
                }
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    ) {
        DatePicker(state = estado, title = { Text(titulo, modifier = Modifier.padding(16.dp)) })
    }
}

internal fun LocalDate.formatarData(): String =
    "%02d/%02d/%04d".format(dayOfMonth, monthValue, year)

private fun FormaPagamento.rotulo(): String = when (this) {
    FormaPagamento.DINHEIRO -> "Dinheiro"
    FormaPagamento.PIX -> "Pix"
    FormaPagamento.CARTAO_CREDITO -> "Cartão de crédito"
    FormaPagamento.CARTAO_DEBITO -> "Cartão de débito"
    FormaPagamento.OUTRO -> "Outro"
}
