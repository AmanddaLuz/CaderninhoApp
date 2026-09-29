package com.caderninho.app.ui.screens.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.StatusPagamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saldo pendente (fiado) de um cliente, calculado a partir das vendas em aberto. */
data class ClienteUiModel(
    val cliente: ClienteEntity,
    val saldoPendente: Double
)

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val repository: CaderninhoRepository
) : ViewModel() {

    val clientes: StateFlow<List<ClienteUiModel>> = repository.observarClientesComVendas()
        .map { lista -> lista.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun adicionarCliente(nome: String, telefone: String, observacao: String = "") {
        if (nome.isBlank()) return
        viewModelScope.launch {
            repository.salvarCliente(
                ClienteEntity(nome = nome.trim(), telefone = telefone.trim(), observacao = observacao.trim())
            )
        }
    }

    fun removerCliente(cliente: ClienteEntity) {
        viewModelScope.launch { repository.removerCliente(cliente) }
    }

    private fun ClienteComVendas.toUiModel(): ClienteUiModel {
        val saldo = vendas.filter { it.status == StatusPagamento.PENDENTE }.sumOf { it.valor }
        return ClienteUiModel(cliente = cliente, saldoPendente = saldo)
    }
}
