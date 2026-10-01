package com.caderninho.app.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject

data class MonthlySummaryUiState(
    val totalReceived: Double = 0.0,
    val totalPending: Double = 0.0,
    val saleCount: Int = 0,
    val byPaymentMethod: Map<PaymentMethod, Double> = emptyMap()
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    repository: LedgerRepository
) : ViewModel() {

    private val currentMonth = YearMonth.now()
    private val zone = ZoneId.systemDefault()
    private val monthStart = currentMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    private val monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

    val state: StateFlow<MonthlySummaryUiState> = repository
        .observeSalesInPeriod(monthStart, monthEnd)
        .map { it.toSummary() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlySummaryUiState())

    private fun List<SaleWithItems>.toSummary(): MonthlySummaryUiState {
        val received = filter { it.sale.status == PaymentStatus.PAID }.sumOf { it.totalCents }
        val pending = filter { it.sale.status == PaymentStatus.PENDING }.sumOf { it.totalCents }
        val byMethod = filter { it.sale.status == PaymentStatus.PAID }
            .groupBy { it.sale.paymentMethod }
            .mapValues { (_, sales) -> sales.sumOf { it.totalCents } / 100.0 }
        return MonthlySummaryUiState(
            totalReceived = received / 100.0,
            totalPending = pending / 100.0,
            saleCount = size,
            byPaymentMethod = byMethod
        )
    }
}
