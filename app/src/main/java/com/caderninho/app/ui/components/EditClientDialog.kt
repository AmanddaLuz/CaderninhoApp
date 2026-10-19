package com.caderninho.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import com.caderninho.app.data.local.ClientEntity

/**
 * Formulário de edição de cliente reutilizado pelas telas de Clientes e de
 * Detalhes do cliente, pré-preenchido com os dados atuais.
 */
@Composable
fun EditClientDialog(
    client: ClientEntity?,
    errorMessage: String?,
    onConfirm: (name: String, phone: String, cpf: String) -> Unit,
    onCancel: () -> Unit
) {
    FormFieldsDialog(
        title = "Editar cliente",
        fields = listOf(
            TextFieldConfig(key = "name", label = "Nome", initialValue = client?.name.orEmpty()),
            TextFieldConfig(
                key = "phone",
                label = "Telefone (WhatsApp)",
                initialValue = client?.phone.orEmpty(),
                keyboardType = KeyboardType.Phone,
                digitsOnly = true,
                maxLength = 11,
                visualTransformation = PhoneVisualTransformation
            ),
            TextFieldConfig(
                key = "cpf",
                label = "CPF (opcional)",
                initialValue = client?.cpf.orEmpty(),
                keyboardType = KeyboardType.Number,
                digitsOnly = true,
                maxLength = 11
            )
        ),
        errorMessage = errorMessage,
        onConfirm = { values ->
            onConfirm(values["name"].orEmpty(), values["phone"].orEmpty(), values["cpf"].orEmpty())
        },
        onCancel = onCancel
    )
}
