package com.caderninho.app.ui.screens.sale

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.ui.components.StatusBadge
import com.caderninho.app.ui.theme.Amber
import com.caderninho.app.ui.theme.LedgerGreen
import com.caderninho.app.util.Formatters
import com.caderninho.app.util.WhatsAppLauncher
import java.time.LocalDate

private data class ClientDetailActions(
    val charge: () -> Unit,
    val markPaid: (SaleEntity) -> Unit,
    val markPending: (SaleEntity) -> Unit,
    val delete: (SaleEntity) -> Unit,
    val filterHistory: (HistoryFilter) -> Unit
)

@Composable
fun ClientDetailScreen(
    viewModel: ClientDetailViewModel = hiltViewModel()
) {
    val data by viewModel.clientWithSales.collectAsState()
    val charge by viewModel.charge.collectAsState()
    val history by viewModel.history.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val context = LocalContext.current
    var showForm by remember { mutableStateOf(false) }
    var saleToMarkPending by remember { mutableStateOf<SaleEntity?>(null) }
    val requestNotification = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    ClientDetailEffects(viewModel, context)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.clearFormError()
                showForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Nova venda")
            }
        }
    ) { padding ->
        ClientDetailContent(
            data = data,
            history = history,
            actions = ClientDetailActions(
                charge = viewModel::startCharge,
                markPaid = viewModel::markAsPaid,
                markPending = { saleToMarkPending = it },
                delete = viewModel::deleteSale,
                filterHistory = viewModel.selectHistoryFilter
            ),
            modifier = Modifier.padding(padding)
        )
    }

    if (showForm) {
        SaleFormDialog(
            errorMessage = formError,
            onConfirm = { items, method, isPaid, dueDate ->
                val saved = viewModel.registerSale(items, method, isPaid, dueDate)
                if (saved) {
                    requestNotificationPermission(context, requestNotification, isPaid)
                    showForm = false
                }
            },
            onCancel = {
                viewModel.clearFormError()
                showForm = false
            }
        )
    }
    if (charge.isOpen) {
        ChargeDialog(
            state = charge,
            onSetSale = viewModel::setSaleForCharge,
            onSelectAll = viewModel::selectAllCharges,
            onConfirm = viewModel::confirmCharge,
            onCancel = viewModel::closeCharge
        )
    }
    saleToMarkPending?.let { sale ->
        DatePickerDialogField(
            title = "Data prevista de pagamento",
            initialDateEpochDay = LocalDate.now().toEpochDay(),
            onConfirm = { data ->
                if (viewModel.markAsPending(sale, data)) saleToMarkPending = null
            },
            onCancel = { saleToMarkPending = null }
        )
    }
}

@Composable
private fun ClientDetailEffects(viewModel: ClientDetailViewModel, context: Context) {
    LaunchedEffect(viewModel) {
        viewModel.chargeDispatches.collect { dispatch ->
            WhatsAppLauncher.sendCharge(context, dispatch.phone, dispatch.message)
        }
    }
    LaunchedEffect(viewModel.openInitialCharge) {
        if (viewModel.openInitialCharge) viewModel.startCharge()
    }
}

@Composable
private fun ClientDetailContent(
    data: ClientWithSales?,
    history: HistoryUiState,
    actions: ClientDetailActions,
    modifier: Modifier = Modifier
) {
    if (data == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando...")
        }
        return
    }
    val pendingBalance = data.sales
        .filter { it.sale.status == PaymentStatus.PENDING }
        .sumOf { it.totalCents }
    Column(modifier = modifier.fillMaxSize()) {
        ClientHeaderCard(
            name = data.client.name,
            phone = data.client.phone,
            pendingBalanceCents = pendingBalance,
            onCharge = actions.charge
        )
        HistoryFilters(
            filter = history.filter,
            onFilterSelected = actions.filterHistory
        )
        SalesList(
            sales = history.sales,
            filter = history.filter,
            actions = actions,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SalesList(
    sales: List<SaleWithItems>,
    filter: HistoryFilter,
    actions: ClientDetailActions,
    modifier: Modifier = Modifier
) {
    if (sales.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                when (filter) {
                    HistoryFilter.ALL -> "Nenhuma venda registrada ainda."
                    HistoryFilter.PENDING -> "Nenhuma venda pendente."
                    HistoryFilter.PAID -> "Nenhuma venda paga."
                }
            )
        }

    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            items(sales, key = { it.sale.id }) { sale ->
                SaleCard(
                    sale = sale,
                    onMarkPaid = { actions.markPaid(sale.sale) },
                    onMarkPending = { actions.markPending(sale.sale) },
                    onDelete = { actions.delete(sale.sale) }
                )
            }
        }
    }
}

@Composable
private fun HistoryFilters(
    filter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Histórico", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            HistoryFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { onFilterSelected(option) },
                    label = { Text(option.label()) }
                )
            }
        }
    }
}

@Composable
private fun ClientHeaderCard(
    name: String,
    phone: String,
    pendingBalanceCents: Long,
    onCharge: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text(phone, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saldo pendente: ${Formatters.currencyFromCents(pendingBalanceCents)}")
                if (pendingBalanceCents > 0L) {
                    TextButton(onClick = onCharge) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Text("Cobrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleCard(
    sale: SaleWithItems,
    onMarkPaid: () -> Unit,
    onMarkPending: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    Card(
        onClick = { showDetails = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SaleCardContent(sale, Modifier.weight(1f))
            Box {
                IconButton(onClick = { isMenuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opções da venda")
                }
                DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
                    val pending = sale.sale.status == PaymentStatus.PENDING
                    DropdownMenuItem(
                        text = { Text(if (pending) "Marcar como pago" else "Marcar como pendente") },
                        onClick = {
                            if (pending) onMarkPaid() else onMarkPending()
                            isMenuOpen = false
                        }
                    )
                    DropdownMenuItem(text = { Text("Remover") }, onClick = {
                        onDelete()
                        isMenuOpen = false
                    })
                }
            }
        }
        if (showDetails) {
            SaleDetailsDialog(sale = sale, onClose = { showDetails = false })
        }
    }
}

@Composable
private fun SaleCardContent(sale: SaleWithItems, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(Formatters.shortDate(sale.sale.createdAt), style = MaterialTheme.typography.labelSmall)
        sale.sale.dueEpochDay?.let {
            Text("Previsto: ${LocalDate.ofEpochDay(it).formatDate()}")
        }
        Text(Formatters.currencyFromCents(sale.totalCents))
        StatusBadge(
            text = if (sale.sale.status == PaymentStatus.PAID) "Pago" else "Pendente",
            backgroundColor = if (sale.sale.status == PaymentStatus.PAID) LedgerGreen else Amber
        )
    }
}

private fun requestNotificationPermission(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    isPaid: Boolean
) {
    val needsPermission = !isPaid &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    if (needsPermission) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
}

private fun HistoryFilter.label(): String = when (this) {
    HistoryFilter.ALL -> "Todos"
    HistoryFilter.PENDING -> "Pendentes"
    HistoryFilter.PAID -> "Pagos"
}
