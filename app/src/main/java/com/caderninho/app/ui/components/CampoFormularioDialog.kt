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
fun CampoFormularioDialog(
    titulo: String,
    campos: List<CampoTexto>,
    mensagemErro: String? = null,
    onConfirmar: (Map<String, String>) -> Unit,
    onCancelar: () -> Unit
) {
    val valores = remember { campos.associate { it.chave to mutableStateOf(it.valorInicial) } }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                campos.forEach { campo ->
                    var valor by valores.getValue(campo.chave)
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { novoValor ->
                            val valorFiltrado = if (campo.somenteDigitos) {
                                novoValor.filter(Char::isDigit)
                            } else {
                                novoValor
                            }
                            if (campo.tamanhoMaximo == null || valorFiltrado.length <= campo.tamanhoMaximo) {
                                valor = valorFiltrado
                            }
                        },
                        label = { Text(campo.rotulo) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = campo.tipoTeclado),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                mensagemErro?.let {
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
                onConfirmar(valores.mapValues { it.value.value })
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

data class CampoTexto(
    val chave: String,
    val rotulo: String,
    val valorInicial: String = "",
    val tipoTeclado: KeyboardType = KeyboardType.Text,
    val somenteDigitos: Boolean = false,
    val tamanhoMaximo: Int? = null
)
