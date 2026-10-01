package com.caderninho.app.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import javax.inject.Inject

enum class SummaryPeriod {
    DAY,
    MONTH
}

data class SummaryUiState(
    val period: SummaryPeriod = SummaryPeriod.MONTH,
    val selectedDate: LocalDate = LocalDate.now(),
    val totalReceivedCents: Long = 0,
    val totalPendingCents: Long = 0,
    val saleCount: Int = 0,
    val byPaymentMethodCents: Map<PaymentMethod, Long> = emptyMap()
)

private data class SummarySelection(
    val period: SummaryPeriod,
    val date: LocalDate
)

private data class PeriodRange(
    val start: Long,
    val endExclusive: Long
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel @Inject constructor(
    private val repository: LedgerRepository
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val selection = MutableStateFlow(
        SummarySelection(SummaryPeriod.MONTH, LocalDate.now())
    )

    val state: StateFlow<SummaryUiState> = selection
        .flatMapLatest { selected ->
            val range = selected.toRange(zone)
            repository.observeSalesForSummary(range.start, range.endExclusive)
                .map { sales -> sales.toSummary(selected) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SummaryUiState()
        )

    fun selectPeriod(period: SummaryPeriod) {
        selection.value = selection.value.copy(period = period)
    }

    fun previousPeriod() {
        selection.value = selection.value.moveBy(-1)
    }

    fun nextPeriod() {
        selection.value = selection.value.moveBy(1)
    }

    fun returnToToday() {
        selection.value = selection.value.copy(date = LocalDate.now())
    }

    private fun SummarySelection.moveBy(amount: Long): SummarySelection =
        copy(
            date = when (period) {
                SummaryPeriod.DAY -> date.plusDays(amount)
                SummaryPeriod.MONTH -> date.plusMonths(amount)
            }
        )

    private fun SummarySelection.toRange(zone: ZoneId): PeriodRange {
        val startDate = when (period) {
            SummaryPeriod.DAY -> date
            SummaryPeriod.MONTH -> YearMonth.from(date).atDay(1)
        }
        val endDate = when (period) {
            SummaryPeriod.DAY -> startDate.plusDays(1)
            SummaryPeriod.MONTH -> startDate.plusMonths(1)
        }
        return PeriodRange(
            start = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            endExclusive = endDate.atStartOfDay(zone).toInstant().toEpochMilli()
        )
    }

    private fun List<SaleWithItems>.toSummary(
        selected: SummarySelection
    ): SummaryUiState {
        val received = filter { it.sale.status == PaymentStatus.PAID }
        val pending = filter { it.sale.status == PaymentStatus.PENDING }
        return SummaryUiState(
            period = selected.period,
            selectedDate = selected.date,
            totalReceivedCents = received.sumOf { it.totalCents },
            totalPendingCents = pending.sumOf { it.totalCents },
            saleCount = size,
            byPaymentMethodCents = received
                .groupBy { it.sale.paymentMethod }
                .mapValues { (_, sales) -> sales.sumOf { it.totalCents } }
        )
    }
}
