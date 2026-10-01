package com.caderninho.app.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.util.CpfValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saldo pendente (fiado) de um cliente, calculado a partir das vendas em aberto. */
data class ClientUiModel(
    val client: ClientEntity,
    val pendingBalanceCents: Long
)

data class ClientsUiState(
    val clients: List<ClientUiModel> = emptyList(),
    val query: String = "",
    val registrationError: String? = null
)

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val repository: LedgerRepository
) : ViewModel() {

    private val allClients = repository.observeClientsWithSales()
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val query = MutableStateFlow("")
    private val registrationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ClientsUiState> =
        combine(allClients, query, registrationError) { clients, term, error ->
            ClientsUiState(
                clients = clients.filter { it.matches(term) },
                query = term,
                registrationError = error
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ClientsUiState()
        )

    fun updateQuery(newQuery: String) {
        query.value = newQuery
    }

    fun addClient(
        name: String,
        phone: String,
        cpf: String,
        notes: String = ""
    ): Boolean {
        val normalizedCpf = CpfValidator.normalize(cpf).ifBlank { null }
        registrationError.value = validateRegistration(name, normalizedCpf)
        if (registrationError.value != null) {
            return false
        }

        viewModelScope.launch {
            repository.saveClient(
                ClientEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    cpf = normalizedCpf,
                    notes = notes.trim()
                )
            )
        }
        return true
    }

    private fun validateRegistration(name: String, cpf: String?): String? = when {
        name.isBlank() -> "Informe o nome do cliente."
        cpf != null && !CpfValidator.validate(cpf) ->
            "Informe um CPF válido ou deixe o campo vazio."
        cpf != null && allClients.value.any { it.client.cpf == cpf } ->
            "Este CPF já está cadastrado."
        else -> null
    }

    fun clearRegistrationError() {
        registrationError.value = null
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch { repository.deleteClient(client) }
    }

    private fun ClientWithSales.toUiModel(): ClientUiModel {
        val saldo = sales
            .filter { it.sale.status == PaymentStatus.PENDING }
            .sumOf { it.totalCents }
        return ClientUiModel(client = client, pendingBalanceCents = saldo)
    }

    private fun ClientUiModel.matches(term: String): Boolean {
        if (term.isBlank()) return true
        val textTerm = term.normalizeText()
        val numericTerm = term.filter(Char::isDigit)
        return client.name.normalizeText().contains(textTerm) ||
            (numericTerm.isNotEmpty() && client.phone.filter(Char::isDigit).contains(numericTerm))
    }

    private fun String.normalizeText(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(ACENTOS_REGEX, "")
            .lowercase()

    private companion object {
        val ACENTOS_REGEX = "\\p{M}+".toRegex()
    }
}
