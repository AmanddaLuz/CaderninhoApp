package com.caderninho.app.ui.screens.venda

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caderninho.app.domain.model.FormaPagamento
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private data class LinhaItemUi(
    val id: Long,
    val descricao: String = "",
    val quantidade: String = "1",
    val valorUnitario: String = ""
)

private data class FormularioVendaEstado(
    val itens: List<LinhaItemUi>,
    val forma: FormaPagamento,
    val jaPago: Boolean,
    val vencimento: Long?
)

private sealed interface FormularioVendaEvento {
    data object AdicionarItem : FormularioVendaEvento
    data class AlterarItem(val item: LinhaItemUi) : FormularioVendaEvento
    data class RemoverItem(val item: LinhaItemUi) : FormularioVendaEvento
    data class AlterarForma(val forma: FormaPagamento) : FormularioVendaEvento
    data class AlterarPago(val pago: Boolean) : FormularioVendaEvento
    data object SelecionarData : FormularioVendaEvento
}

private class FormularioVendaEditor {
    val itens = mutableStateListOf(LinhaItemUi(id = 0))
    private var proximoId by mutableLongStateOf(1)
    var forma by mutableStateOf(FormaPagamento.DINHEIRO)
    var jaPago by mutableStateOf(false)
    var vencimento by mutableStateOf<Long?>(null)
    var mostrarData by mutableStateOf(false)

    val estado: FormularioVendaEstado
        get() = FormularioVendaEstado(itens, forma, jaPago, vencimento)

    fun aplicar(evento: FormularioVendaEvento) {
        when (evento) {
            FormularioVendaEvento.AdicionarItem -> itens += LinhaItemUi(id = proximoId++)
            is FormularioVendaEvento.AlterarItem ->
                itens[itens.indexOfFirst { it.id == evento.item.id }] = evento.item
            is FormularioVendaEvento.RemoverItem -> itens.remove(evento.item)
            is FormularioVendaEvento.AlterarForma -> forma = evento.forma
            is FormularioVendaEvento.AlterarPago -> {
                jaPago = evento.pago
                if (evento.pago) vencimento = null
            }
            FormularioVendaEvento.SelecionarData -> mostrarData = true
        }
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
        text = {
            ConteudoFormularioVenda(
                estado = editor.estado,
                mensagemErro = mensagemErro,
                onEvento = editor::aplicar
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmar(
                    editor.itens.map {
                        ItemVendaFormulario(it.descricao, it.quantidade, it.valorUnitario)
                    },
                    editor.forma,
                    editor.jaPago,
                    editor.vencimento
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )

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
private fun ConteudoFormularioVenda(
    estado: FormularioVendaEstado,
    mensagemErro: String?,
    onEvento: (FormularioVendaEvento) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(estado.itens, key = LinhaItemUi::id) { item ->
            ItemVendaCampos(
                item,
                podeRemover = estado.itens.size > 1,
                onAlterar = { onEvento(FormularioVendaEvento.AlterarItem(it)) },
                onRemover = { onEvento(FormularioVendaEvento.RemoverItem(item)) }
            )
        }
        item {
            TextButton(onClick = { onEvento(FormularioVendaEvento.AdicionarItem) }) {
                Icon(Icons.Filled.Add, null)
                Text("Adicionar item")
            }
        }
        item {
            SeletorFormaPagamento(estado.forma) {
                onEvento(FormularioVendaEvento.AlterarForma(it))
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Já foi pago agora?")
                Switch(
                    checked = estado.jaPago,
                    onCheckedChange = { onEvento(FormularioVendaEvento.AlterarPago(it)) }
                )
            }
        }
        if (!estado.jaPago) {
            item {
                TextButton(onClick = { onEvento(FormularioVendaEvento.SelecionarData) }) {
                    Text(
                        estado.vencimento?.let {
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
private fun ItemVendaCampos(
    item: LinhaItemUi,
    podeRemover: Boolean,
    onAlterar: (LinhaItemUi) -> Unit,
    onRemover: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = item.descricao,
                onValueChange = { onAlterar(item.copy(descricao = it)) },
                label = { Text("Descrição") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            if (podeRemover) {
                IconButton(onClick = onRemover) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remover item")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CampoDecimal(item.quantidade, "Quantidade", Modifier.weight(1f)) {
                onAlterar(item.copy(quantidade = it))
            }
            CampoDecimal(item.valorUnitario, "Valor unitário", Modifier.weight(1f)) {
                onAlterar(item.copy(valorUnitario = it))
            }
        }
    }
}

@Composable
private fun CampoDecimal(
    valor: String,
    rotulo: String,
    modifier: Modifier,
    onAlterar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onAlterar,
        label = { Text(rotulo) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun SeletorFormaPagamento(
    forma: FormaPagamento,
    onFormaAlterada: (FormaPagamento) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = { aberto = true }) {
            Text("Forma de pagamento: ${forma.rotulo()}")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            FormaPagamento.entries.forEach { opcao ->
                DropdownMenuItem(text = { Text(opcao.rotulo()) }, onClick = {
                    onFormaAlterada(opcao)
                    aberto = false
                })
            }
        }
    }
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
                    onConfirmar(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay())
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
