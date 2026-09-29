package com.caderninho.app.ui.screens.venda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteDetalheViewModel @Inject constructor(
    private val repository: CaderninhoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val clienteId: Long = checkNotNull(savedStateHandle["clienteId"])

    val clienteComVendas: StateFlow<ClienteComVendas?> =
        repository.observarClienteComVendas(clienteId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun registrarVenda(descricao: String, valor: Double, forma: FormaPagamento, jaPago: Boolean) {
        if (descricao.isBlank() || valor <= 0.0) return
        viewModelScope.launch {
            repository.salvarVenda(
                VendaEntity(
                    clienteId = clienteId,
                    descricao = descricao.trim(),
                    valor = valor,
                    formaPagamento = forma,
                    status = if (jaPago) StatusPagamento.PAGO else StatusPagamento.PENDENTE,
                    pagoEm = if (jaPago) System.currentTimeMillis() else null
                )
            )
        }
    }

    fun marcarComoPago(venda: VendaEntity) {
        viewModelScope.launch {
            repository.marcarStatus(venda.id, StatusPagamento.PAGO, System.currentTimeMillis())
        }
    }

    fun marcarComoPendente(venda: VendaEntity) {
        viewModelScope.launch {
            repository.marcarStatus(venda.id, StatusPagamento.PENDENTE, null)
        }
    }

    fun removerVenda(venda: VendaEntity) {
        viewModelScope.launch { repository.removerVenda(venda) }
    }
}
