package com.caderninho.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Diálogo genérico para capturar campos de texto e confirmar/cancelar.
 * Reutilizável em qualquer formulário simples do app (cliente, venda, etc).
 */
@Composable
fun FormFieldsDialog(
    title: String,
    fields: List<TextFieldConfig>,
    errorMessage: String? = null,
    onConfirm: (Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    val values = remember { fields.associate { it.key to mutableStateOf(it.initialValue) } }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                fields.forEach { field ->
                    var value by values.getValue(field.key)
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            val filteredValue = if (field.digitsOnly) {
                                newValue.filter(Char::isDigit)
                            } else {
                                newValue
                            }
                            if (field.maxLength == null || filteredValue.length <= field.maxLength) {
                                value = filteredValue
                            }
                        },
                        label = { Text(field.label) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                errorMessage?.let {
                    Text(
                        text = it,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(values.mapValues { it.value.value })
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancelar") }
        }
    )
}

data class TextFieldConfig(
    val key: String,
    val label: String,
    val initialValue: String = "",
    val keyboardType: KeyboardType = KeyboardType.Text,
    val digitsOnly: Boolean = false,
    val maxLength: Int? = null
)
