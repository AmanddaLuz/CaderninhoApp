package com.caderninho.app.ui.screens.sale

import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.util.CpfValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Agrupa a edição e exclusão do cliente exibido em [ClientDetailScreen],
 * mantendo o [ClientDetailViewModel] abaixo do limite de funções do Detekt.
 */
class ClientEditController(
    private val repository: LedgerRepository,
    private val currentClient: () -> ClientEntity?,
    private val scope: CoroutineScope
) {
    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted: SharedFlow<Unit> = _deleted.asSharedFlow()

    fun update(name: String, phone: String, cpf: String): Boolean {
        val normalizedCpf = CpfValidator.normalize(cpf).ifBlank { null }
        _formError.value = validate(name, normalizedCpf)
        val current = currentClient()
        return when {
            _formError.value != null -> false
            current == null -> false
            else -> {
                persist(current, name, phone, normalizedCpf)
                true
            }
        }
    }

    private fun persist(current: ClientEntity, name: String, phone: String, cpf: String?) {
        scope.launch {
            runCatching {
                repository.saveClient(
                    current.copy(
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

    fun clearError() {
        _formError.value = null
    }

    fun delete() {
        val current = currentClient() ?: return
        scope.launch {
            repository.deleteClient(current)
            _deleted.emit(Unit)
        }
    }

    private fun validate(name: String, cpf: String?): String? = when {
        name.isBlank() -> "Informe o nome do cliente."
        cpf != null && !CpfValidator.validate(cpf) ->
            "Informe um CPF válido ou deixe o campo vazio."
        else -> null
    }
}
