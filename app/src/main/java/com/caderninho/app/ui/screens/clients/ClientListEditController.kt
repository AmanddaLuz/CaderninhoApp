package com.caderninho.app.ui.screens.clients

import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.util.CpfValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Edição de cliente a partir da tela de Clientes, onde o cliente alvo é
 * informado a cada chamada (diferente de
 * [com.caderninho.app.ui.screens.sale.ClientEditController], vinculado a um
 * único cliente na tela de detalhes).
 */
class ClientListEditController(
    private val repository: LedgerRepository,
    private val existingClients: () -> List<ClientEntity>,
    private val scope: CoroutineScope
) {
    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError

    fun update(client: ClientEntity, name: String, phone: String, cpf: String): Boolean {
        val normalizedCpf = CpfValidator.normalize(cpf).ifBlank { null }
        _formError.value = validate(client.id, name, normalizedCpf)
        return if (_formError.value != null) {
            false
        } else {
            persist(client, name, phone, normalizedCpf)
            true
        }
    }

    fun clearError() {
        _formError.value = null
    }

    private fun persist(client: ClientEntity, name: String, phone: String, cpf: String?) {
        scope.launch {
            runCatching {
                repository.saveClient(
                    client.copy(
                        name = name.trim(),
                        phone = phone.filter(Char::isDigit),
                        cpf = cpf
                    )
                )
            }.onFailure {
                _formError.value = "Este CPF já está cadastrado para outro cliente."
            }
        }
    }

    private fun validate(clientId: Long, name: String, cpf: String?): String? = when {
        name.isBlank() -> "Informe o nome do cliente."
        cpf != null && !CpfValidator.validate(cpf) ->
            "Informe um CPF válido ou deixe o campo vazio."
        cpf != null && existingClients().any { it.cpf == cpf && it.id != clientId } ->
            "Este CPF já está cadastrado para outro cliente."
        else -> null
    }
}
