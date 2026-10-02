package com.caderninho.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val clientCount: Int = 0,
    val pendingSaleCount: Int = 0,
    val overdueSaleCount: Int = 0,
    val dueTodayCount: Int = 0,
    val pendingTotalCents: Long = 0,
    val dueTodayTotalCents: Long = 0,
    val valuesVisible: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: LedgerRepository
) : ViewModel() {

    private val valuesVisible = MutableStateFlow(false)

    val state: StateFlow<HomeUiState> = combine(
        repository.observeClientsWithSales(),
        valuesVisible
    ) { clients, isVisible ->
        clients.toHomeState(isVisible)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )

    fun toggleValuesVisibility() {
        valuesVisible.value = !valuesVisible.value
    }

    private fun List<ClientWithSales>.toHomeState(isVisible: Boolean): HomeUiState {
        val today = LocalDate.now().toEpochDay()
        val pendingSales = flatMap(ClientWithSales::sales)
            .filter { it.sale.status == PaymentStatus.PENDING }
        val dueToday = pendingSales.filter { it.sale.dueEpochDay == today }
        return HomeUiState(
            clientCount = size,
            pendingSaleCount = pendingSales.size,
            overdueSaleCount = pendingSales.count {
                it.sale.dueEpochDay?.let { dueDate -> dueDate < today } == true
            },
            dueTodayCount = dueToday.size,
            pendingTotalCents = pendingSales.sumOf { it.totalCents },
            dueTodayTotalCents = dueToday.sumOf { it.totalCents },
            valuesVisible = isVisible
        )
    }
}
