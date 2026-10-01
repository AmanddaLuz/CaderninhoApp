package com.caderninho.app.ui.screens.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.notification.ChargeReminderCoordinator
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

data class SaleItemForm(
    val description: String,
    val quantity: String,
    val unitValue: String
)

data class ChargeSaleUiModel(
    val sale: SaleWithItems,
    val isSelected: Boolean
)

data class ChargeUiState(
    val isOpen: Boolean = false,
    val sales: List<ChargeSaleUiModel> = emptyList(),
    val selectedTotalCents: Long = 0,
    val allSelected: Boolean = false
)

data class ChargeDispatch(
    val phone: String,
    val message: String
)

enum class HistoryFilter {
    ALL,
    PENDING,
    PAID
}

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val sales: List<SaleWithItems> = emptyList()
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val reminderCoordinator: ChargeReminderCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val clientId: Long = checkNotNull(savedStateHandle["clientId"])
    val openInitialCharge: Boolean = savedStateHandle["charge"] ?: false

    val clientWithSales: StateFlow<ClientWithSales?> =
        repository.observeClientWithSales(clientId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val isChargeOpen = MutableStateFlow(false)
    private val selectedSaleIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError

    private val _chargeDispatches = MutableSharedFlow<ChargeDispatch>()
    val chargeDispatches: SharedFlow<ChargeDispatch> = _chargeDispatches.asSharedFlow()

    val charge: StateFlow<ChargeUiState> =
        combine(clientWithSales, isChargeOpen, selectedSaleIds) { data, isOpen, selectedSales ->
            val pendingSales = data?.sales.orEmpty()
                .filter { it.sale.status == PaymentStatus.PENDING }
            val saleUiModels = pendingSales.map {
                ChargeSaleUiModel(it, it.sale.id in selectedSales)
            }
            ChargeUiState(
                isOpen = isOpen,
                sales = saleUiModels,
                selectedTotalCents = saleUiModels
                    .filter(ChargeSaleUiModel::isSelected)
                    .sumOf { it.sale.totalCents },
                allSelected = saleUiModels.isNotEmpty() &&
                    saleUiModels.all(ChargeSaleUiModel::isSelected)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChargeUiState())

    private val historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val history: StateFlow<HistoryUiState> =
        combine(clientWithSales, historyFilter) { data, filter ->
            val sales = data?.sales.orEmpty()
                .asSequence()
                .filter { sale ->
                    when (filter) {
                        HistoryFilter.ALL -> true
                        HistoryFilter.PENDING ->
                            sale.sale.status == PaymentStatus.PENDING
                        HistoryFilter.PAID ->
                            sale.sale.status == PaymentStatus.PAID
                    }
                }
                .sortedByDescending { it.sale.createdAt }
                .toList()
            HistoryUiState(filter = filter, sales = sales)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HistoryUiState()
        )

    val selectHistoryFilter: (HistoryFilter) -> Unit = { filter ->
        historyFilter.value = filter
    }

    fun registerSale(
        formItems: List<SaleItemForm>,
        method: PaymentMethod,
        isPaid: Boolean,
        dueEpochDay: Long?
    ): Boolean {
        val validation = SaleRules.validateItems(formItems)
        _formError.value = validation.error ?: validateSaleDueDate(isPaid, dueEpochDay)
        if (_formError.value != null) {
            return false
        }

        viewModelScope.launch {
            repository.saveSale(
                sale = SaleEntity(
                    clientId = clientId,
                    paymentMethod = method,
                    status = if (isPaid) PaymentStatus.PAID else PaymentStatus.PENDING,
                    dueEpochDay = dueEpochDay,
                    paidAt = if (isPaid) System.currentTimeMillis() else null
                ),
                items = validation.items
            )
            if (!isPaid) reminderCoordinator.reconcile(clientId, dueEpochDay)
        }
        return true
    }

    fun markAsPaid(sale: SaleEntity) {
        viewModelScope.launch {
            repository.updateStatus(
                saleId = sale.id,
                status = PaymentStatus.PAID,
                paidAt = System.currentTimeMillis(),
                dueEpochDay = sale.dueEpochDay
            )
            reminderCoordinator.reconcile(clientId, sale.dueEpochDay)
        }
    }

    fun markAsPending(sale: SaleEntity, dueEpochDay: Long): Boolean {
        if (dueEpochDay < LocalDate.now().toEpochDay()) return false
        viewModelScope.launch {
            repository.updateStatus(
                saleId = sale.id,
                status = PaymentStatus.PENDING,
                paidAt = null,
                dueEpochDay = dueEpochDay
            )
            reminderCoordinator.reconcile(clientId, dueEpochDay)
        }
        return true
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
            reminderCoordinator.reconcile(clientId, sale.dueEpochDay)
        }
    }

    fun startCharge() {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val data = clientWithSales.filterNotNull().first()
            selectedSaleIds.value = data.sales
                .filter {
                    it.sale.status == PaymentStatus.PENDING &&
                        it.sale.dueEpochDay?.let { dueDate ->
                            dueDate <= today
                        } == true
                }
                .mapTo(mutableSetOf()) { it.sale.id }
            isChargeOpen.value = true
        }
    }

    fun closeCharge() {
        isChargeOpen.value = false
        selectedSaleIds.value = emptySet()
    }

    fun setSaleForCharge(saleId: Long, isSelected: Boolean) {
        selectedSaleIds.value = selectedSaleIds.value.toMutableSet().apply {
            if (isSelected) add(saleId) else remove(saleId)
        }
    }

    fun selectAllCharges(select: Boolean) {
        selectedSaleIds.value = if (select) {
            clientWithSales.value?.sales.orEmpty()
                .filter { it.sale.status == PaymentStatus.PENDING }
                .mapTo(mutableSetOf()) { it.sale.id }
        } else {
            emptySet()
        }
    }

    fun confirmCharge() {
        val data = clientWithSales.value ?: return
        val selectedSales = data.sales.filter { it.sale.id in selectedSaleIds.value }
        if (selectedSales.isEmpty()) return
        val message = SaleRules.createChargeMessage(data.client.name, selectedSales)
        viewModelScope.launch {
            _chargeDispatches.emit(ChargeDispatch(data.client.phone, message))
            closeCharge()
        }
    }

    fun clearFormError() {
        _formError.value = null
    }

}

private fun validateSaleDueDate(isPaid: Boolean, dueEpochDay: Long?): String? = when {
    !isPaid && dueEpochDay == null -> "Informe a data prevista de pagamento."
    dueEpochDay != null && dueEpochDay < LocalDate.now().toEpochDay() ->
        "A data prevista não pode estar no passado."
    else -> null
}
