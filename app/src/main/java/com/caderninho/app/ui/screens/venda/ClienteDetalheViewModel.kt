package com.caderninho.app.ui.screens.venda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.notification.LembreteCobrancaCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemVendaFormulario(
    val descricao: String,
    val quantidade: String,
    val valorUnitario: String
)

data class VendaCobrancaUiModel(
    val venda: VendaComItens,
    val selecionada: Boolean
)

data class CobrancaUiState(
    val aberta: Boolean = false,
    val vendas: List<VendaCobrancaUiModel> = emptyList(),
    val totalSelecionadoCentavos: Long = 0,
    val todasSelecionadas: Boolean = false
)

data class EnvioCobranca(
    val telefone: String,
    val mensagem: String
)

@HiltViewModel
class ClienteDetalheViewModel @Inject constructor(
    private val repository: CaderninhoRepository,
    private val lembretes: LembreteCobrancaCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val clienteId: Long = checkNotNull(savedStateHandle["clienteId"])
    val abrirCobrancaInicial: Boolean = savedStateHandle["cobrar"] ?: false

    val clienteComVendas: StateFlow<ClienteComVendas?> =
        repository.observarClienteComVendas(clienteId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val cobrancaAberta = MutableStateFlow(false)
    private val vendasSelecionadas = MutableStateFlow<Set<Long>>(emptySet())
    private val _erroFormulario = MutableStateFlow<String?>(null)
    val erroFormulario: StateFlow<String?> = _erroFormulario

    private val _enviosCobranca = MutableSharedFlow<EnvioCobranca>()
    val enviosCobranca: SharedFlow<EnvioCobranca> = _enviosCobranca.asSharedFlow()

    val cobranca: StateFlow<CobrancaUiState> =
        combine(clienteComVendas, cobrancaAberta, vendasSelecionadas) { dados, aberta, selecionadas ->
            val pendentes = dados?.vendas.orEmpty()
                .filter { it.venda.status == StatusPagamento.PENDENTE }
            val vendasUi = pendentes.map {
                VendaCobrancaUiModel(it, it.venda.id in selecionadas)
            }
            CobrancaUiState(
                aberta = aberta,
                vendas = vendasUi,
                totalSelecionadoCentavos = vendasUi
                    .filter(VendaCobrancaUiModel::selecionada)
                    .sumOf { it.venda.totalCentavos },
                todasSelecionadas = vendasUi.isNotEmpty() && vendasUi.all(VendaCobrancaUiModel::selecionada)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CobrancaUiState())

    fun registrarVenda(
        itensFormulario: List<ItemVendaFormulario>,
        forma: FormaPagamento,
        jaPago: Boolean,
        vencimentoEpochDay: Long?
    ): Boolean {
        val validacao = VendaRegras.validarItens(itensFormulario)
        _erroFormulario.value = validacao.erro ?: validarVencimentoVenda(jaPago, vencimentoEpochDay)
        if (_erroFormulario.value != null) {
            return false
        }

        viewModelScope.launch {
            repository.salvarVenda(
                venda = VendaEntity(
                    clienteId = clienteId,
                    formaPagamento = forma,
                    status = if (jaPago) StatusPagamento.PAGO else StatusPagamento.PENDENTE,
                    vencimentoEpochDay = vencimentoEpochDay,
                    pagoEm = if (jaPago) System.currentTimeMillis() else null
                ),
                itens = validacao.itens
            )
            if (!jaPago) lembretes.reconciliar(clienteId, vencimentoEpochDay)
        }
        return true
    }

    fun marcarComoPago(venda: VendaEntity) {
        viewModelScope.launch {
            repository.marcarStatus(
                vendaId = venda.id,
                status = StatusPagamento.PAGO,
                pagoEm = System.currentTimeMillis(),
                vencimentoEpochDay = venda.vencimentoEpochDay
            )
            lembretes.reconciliar(clienteId, venda.vencimentoEpochDay)
        }
    }

    fun marcarComoPendente(venda: VendaEntity, vencimentoEpochDay: Long): Boolean {
        if (vencimentoEpochDay < LocalDate.now().toEpochDay()) return false
        viewModelScope.launch {
            repository.marcarStatus(
                vendaId = venda.id,
                status = StatusPagamento.PENDENTE,
                pagoEm = null,
                vencimentoEpochDay = vencimentoEpochDay
            )
            lembretes.reconciliar(clienteId, vencimentoEpochDay)
        }
        return true
    }

    fun removerVenda(venda: VendaEntity) {
        viewModelScope.launch {
            repository.removerVenda(venda)
            lembretes.reconciliar(clienteId, venda.vencimentoEpochDay)
        }
    }

    fun iniciarCobranca() {
        viewModelScope.launch {
            val hoje = LocalDate.now().toEpochDay()
            val dados = clienteComVendas.filterNotNull().first()
            vendasSelecionadas.value = dados.vendas
                .filter {
                    it.venda.status == StatusPagamento.PENDENTE &&
                        it.venda.vencimentoEpochDay?.let { data -> data <= hoje } == true
                }
                .mapTo(mutableSetOf()) { it.venda.id }
            cobrancaAberta.value = true
        }
    }

    fun fecharCobranca() {
        cobrancaAberta.value = false
        vendasSelecionadas.value = emptySet()
    }

    fun alternarVendaCobranca(vendaId: Long) {
        vendasSelecionadas.value = vendasSelecionadas.value.toMutableSet().apply {
            if (!add(vendaId)) remove(vendaId)
        }
    }

    fun selecionarTodasCobrancas(selecionar: Boolean) {
        vendasSelecionadas.value = if (selecionar) {
            clienteComVendas.value?.vendas.orEmpty()
                .filter { it.venda.status == StatusPagamento.PENDENTE }
                .mapTo(mutableSetOf()) { it.venda.id }
        } else {
            emptySet()
        }
    }

    fun confirmarCobranca() {
        val dados = clienteComVendas.value ?: return
        val selecionadas = dados.vendas.filter { it.venda.id in vendasSelecionadas.value }
        if (selecionadas.isEmpty()) return
        val mensagem = VendaRegras.criarMensagemCobranca(dados.cliente.nome, selecionadas)
        viewModelScope.launch {
            _enviosCobranca.emit(EnvioCobranca(dados.cliente.telefone, mensagem))
            fecharCobranca()
        }
    }

    fun limparErroFormulario() {
        _erroFormulario.value = null
    }

}

private fun validarVencimentoVenda(jaPago: Boolean, vencimentoEpochDay: Long?): String? = when {
    !jaPago && vencimentoEpochDay == null -> "Informe a data prevista de pagamento."
    vencimentoEpochDay != null && vencimentoEpochDay < LocalDate.now().toEpochDay() ->
        "A data prevista não pode estar no passado."
    else -> null
}
