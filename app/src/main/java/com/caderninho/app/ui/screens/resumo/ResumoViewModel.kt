package com.caderninho.app.ui.screens.resumo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.VendaEntity
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

    private fun List<VendaEntity>.toResumo(): ResumoMensalUiState {
        val recebido = filter { it.status == StatusPagamento.PAGO }.sumOf { it.valor }
        val pendente = filter { it.status == StatusPagamento.PENDENTE }.sumOf { it.valor }
        val porForma = filter { it.status == StatusPagamento.PAGO }
            .groupBy { it.formaPagamento }
            .mapValues { (_, vendas) -> vendas.sumOf { it.valor } }
        return ResumoMensalUiState(
            totalRecebido = recebido,
            totalPendente = pendente,
            quantidadeVendas = size,
            porFormaPagamento = porForma
        )
    }
}
