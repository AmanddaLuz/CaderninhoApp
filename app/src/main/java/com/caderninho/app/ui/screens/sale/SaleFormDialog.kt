package com.caderninho.app.ui.screens.sale

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
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.util.NumericValues
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private data class SaleItemUi(
    val id: Long,
    val description: String = "",
    val quantity: String = "1",
    val unitValue: String = "R$ 0,00"
)

private class SaleFormEditor {
    val items = mutableStateListOf<SaleItemUi>()
    private var nextId by mutableLongStateOf(1)
    var itemBeingEdited by mutableStateOf<SaleItemUi?>(null)
    var method by mutableStateOf(PaymentMethod.CASH)
    var isPaid by mutableStateOf(false)
    var dueDate by mutableStateOf<Long?>(null)
    var showDate by mutableStateOf(false)

    fun addItem() {
        itemBeingEdited = SaleItemUi(id = nextId++)
    }

    fun saveItem(item: SaleItemUi) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) items[index] = item else items += item
        itemBeingEdited = null
    }

    fun removeItem(item: SaleItemUi) {
        items.remove(item)
    }

    fun formItems(): List<SaleItemForm> = items.map {
        SaleItemForm(it.description, it.quantity, it.unitValue)
    }
}

@Composable
internal fun SaleFormDialog(
    errorMessage: String?,
    onConfirm: (List<SaleItemForm>, PaymentMethod, Boolean, Long?) -> Unit,
    onCancel: () -> Unit
) {
    val editor = remember { SaleFormEditor() }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Nova venda ou serviço") },
        text = { SaleFormContent(editor, errorMessage) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    editor.formItems(),
                    editor.method,
                    editor.isPaid,
                    editor.dueDate
                )
            }) { Text("Salvar venda") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
    SaleFormDialogs(editor)
}

@Composable
private fun SaleFormContent(
    editor: SaleFormEditor,
    errorMessage: String?
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (editor.items.isEmpty()) {
            item {
                Text(
                    "Nenhum item adicionado.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        items(editor.items, key = SaleItemUi::id) { item ->
            SaleItemSummary(
                item = item,
                onEdit = { editor.itemBeingEdited = item },
                onDelete = { editor.removeItem(item) }
            )
        }
        item {
            TextButton(onClick = editor::addItem) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Adicionar item")
            }
        }
        item { PaymentMethodSelector(editor) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Já foi pago agora?")
                Switch(
                    checked = editor.isPaid,
                    onCheckedChange = {
                        editor.isPaid = it
                        if (it) editor.dueDate = null
                    }
                )
            }
        }
        if (!editor.isPaid) {
            item {
                TextButton(onClick = { editor.showDate = true }) {
                    Text(
                        editor.dueDate?.let {
                            "Pagamento previsto: ${LocalDate.ofEpochDay(it).formatDate()}"
                        } ?: "Selecionar data prevista de pagamento"
                    )
                }
            }
        }
        errorMessage?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun SaleItemSummary(
    item: SaleItemUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.description, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.quantity} × ${item.unitValue}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remover item")
            }
        }
    }
}

@Composable
private fun PaymentMethodSelector(editor: SaleFormEditor) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { isOpen = true }) {
            Text("Forma de pagamento: ${editor.method.label()}")
        }
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }) {
            PaymentMethod.entries.forEach { option ->
                DropdownMenuItem(text = { Text(option.label()) }, onClick = {
                    editor.method = option
                    isOpen = false
                })
            }
        }
    }
}

@Composable
private fun SaleFormDialogs(editor: SaleFormEditor) {
    editor.itemBeingEdited?.let { item ->
        SaleItemDialog(
            item = item,
            onSave = editor::saveItem,
            onCancel = { editor.itemBeingEdited = null }
        )
    }
    if (editor.showDate) {
        DatePickerDialogField(
            title = "Data prevista de pagamento",
            initialDateEpochDay = editor.dueDate ?: LocalDate.now().toEpochDay(),
            onConfirm = {
                editor.dueDate = it
                editor.showDate = false
            },
            onCancel = { editor.showDate = false }
        )
    }
}

@Composable
private fun SaleItemDialog(
    item: SaleItemUi,
    onSave: (SaleItemUi) -> Unit,
    onCancel: () -> Unit
) {
    var description by remember(item.id) { mutableStateOf(item.description) }
    var quantity by remember(item.id) { mutableStateOf(item.quantity) }
    var value by remember(item.id) {
        mutableStateOf(
            TextFieldValue(
                text = item.unitValue,
                selection = TextRange(item.unitValue.length)
            )
        )
    }
    val valido = description.isNotBlank() &&
        NumericValues.quantity(quantity) != null &&
        NumericValues.cents(value.text) != null

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (item.description.isBlank()) "Adicionar item" else "Editar item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        val formatado = NumericValues.formatCurrencyInput(it.text)
                        value = TextFieldValue(formatado, TextRange(formatado.length))
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
                    onSave(
                        item.copy(
                            description = description.trim(),
                            quantity = quantity,
                            unitValue = value.text
                        )
                    )
                }
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerDialogField(
    title: String,
    initialDateEpochDay: Long,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val initialMillis = LocalDate.ofEpochDay(initialDateEpochDay)
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onConfirm(
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                    )
                }
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    ) {
        DatePicker(state = state, title = { Text(title, modifier = Modifier.padding(16.dp)) })
    }
}

internal fun LocalDate.formatDate(): String =
    "%02d/%02d/%04d".format(dayOfMonth, monthValue, year)

private fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Dinheiro"
    PaymentMethod.PIX -> "Pix"
    PaymentMethod.CREDIT_CARD -> "Cartão de crédito"
    PaymentMethod.DEBIT_CARD -> "Cartão de débito"
    PaymentMethod.OTHER -> "Outro"
}
