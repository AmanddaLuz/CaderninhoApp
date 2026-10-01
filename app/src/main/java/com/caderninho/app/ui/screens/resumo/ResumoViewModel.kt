package com.caderninho.app.ui.screens.resumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject

data class ResumoMensalUiState(
    val totalRecebido: Double = 0.0,
    val totalPendente: Double = 0.0,
    val quantidadeVendas: Int = 0,
    val porFormaPagamento: Map<FormaPagamento, Double> = emptyMap()
)

@HiltViewModel
class ResumoViewModel @Inject constructor(
    repository: CaderninhoRepository
) : ViewModel() {

    private val mesAtual = YearMonth.now()
    private val zona = ZoneId.systemDefault()
    private val inicioMes = mesAtual.atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()
    private val fimMes = mesAtual.atEndOfMonth().atTime(23, 59, 59).atZone(zona).toInstant().toEpochMilli()

    val estado: StateFlow<ResumoMensalUiState> = repository
        .observarVendasNoPeriodo(inicioMes, fimMes)
        .map { it.toResumo() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumoMensalUiState())

    private fun List<VendaComItens>.toResumo(): ResumoMensalUiState {
        val recebido = filter { it.venda.status == StatusPagamento.PAGO }.sumOf { it.totalCentavos }
        val pendente = filter { it.venda.status == StatusPagamento.PENDENTE }.sumOf { it.totalCentavos }
        val porForma = filter { it.venda.status == StatusPagamento.PAGO }
            .groupBy { it.venda.formaPagamento }
            .mapValues { (_, vendas) -> vendas.sumOf { it.totalCentavos } / 100.0 }
        return ResumoMensalUiState(
            totalRecebido = recebido / 100.0,
            totalPendente = pendente / 100.0,
            quantidadeVendas = size,
            porFormaPagamento = porForma
        )
    }
}
