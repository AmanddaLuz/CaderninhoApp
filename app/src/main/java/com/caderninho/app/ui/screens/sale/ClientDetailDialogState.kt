package com.caderninho.app.ui.screens.sale

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.ui.components.EditClientDialog
import java.time.LocalDate

/**
 * Estado dos diálogos exibidos em [ClientDetailScreen]: formulário de venda,
 * edição/exclusão de cliente e ajuste de datas de pendência.
 */
class ClientDetailDialogState {
    var showForm by mutableStateOf(false)
    var showEditClient by mutableStateOf(false)
    var showDeleteConfirm by mutableStateOf(false)
    var saleToMarkPending by mutableStateOf<SaleEntity?>(null)
    var saleToReschedule by mutableStateOf<SaleEntity?>(null)
}

@Composable
fun rememberClientDetailDialogState() = remember { ClientDetailDialogState() }

@Composable
fun ClientDetailDialogs(
    viewModel: ClientDetailViewModel,
    data: ClientWithSales?,
    charge: ChargeUiState,
    state: ClientDetailDialogState,
    context: Context
) {
    ClientFormDialogs(viewModel, data, state, context)
    SaleAdjustmentDialogs(viewModel, charge, state)
}

@Composable
private fun ClientFormDialogs(
    viewModel: ClientDetailViewModel,
    data: ClientWithSales?,
    state: ClientDetailDialogState,
    context: Context
) {
    val formError by viewModel.formError.collectAsState()
    val clientFormError by viewModel.clientEditor.formError.collectAsState()
    val requestNotification = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    if (state.showForm) {
        SaleFormDialog(
            errorMessage = formError,
            onConfirm = { items, method, isPaid, dueDate ->
                val saved = viewModel.registerSale(items, method, isPaid, dueDate)
                if (saved) {
                    requestNotificationPermission(context, requestNotification, isPaid)
                    state.showForm = false
                }
            },
            onCancel = {
                viewModel.clearFormError()
                state.showForm = false
            }
        )
    }
    if (state.showEditClient) {
        EditClientDialog(
            client = data?.client,
            errorMessage = clientFormError,
            onConfirm = { name, phone, cpf ->
                if (viewModel.clientEditor.update(name, phone, cpf)) state.showEditClient = false
            },
            onCancel = {
                viewModel.clientEditor.clearError()
                state.showEditClient = false
            }
        )
    }
    if (state.showDeleteConfirm) {
        DeleteClientConfirmDialog(
            clientName = data?.client?.name.orEmpty(),
            onConfirm = {
                viewModel.clientEditor.delete()
                state.showDeleteConfirm = false
            },
            onCancel = { state.showDeleteConfirm = false }
        )
    }
}

@Composable
private fun SaleAdjustmentDialogs(
    viewModel: ClientDetailViewModel,
    charge: ChargeUiState,
    state: ClientDetailDialogState
) {
    if (charge.isOpen) {
        ChargeDialog(
            state = charge,
            onSetSale = viewModel::setSaleForCharge,
            onSelectAll = viewModel::selectAllCharges,
            onConfirm = viewModel::confirmCharge,
            onCancel = viewModel::closeCharge
        )
    }
    state.saleToMarkPending?.let { sale ->
        DatePickerDialogField(
            title = "Data prevista de pagamento",
            initialDateEpochDay = LocalDate.now().toEpochDay(),
            onConfirm = { newDate ->
                if (viewModel.markAsPending(sale, newDate)) state.saleToMarkPending = null
            },
            onCancel = { state.saleToMarkPending = null }
        )
    }
    state.saleToReschedule?.let { sale ->
        RescheduleDateDialog(sale, viewModel::rescheduleDueDate) {
            state.saleToReschedule = null
        }
    }
}

@Composable
private fun DeleteClientConfirmDialog(
    clientName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Excluir cliente") },
        text = {
            Text("Tem certeza que deseja excluir $clientName? Todas as vendas registradas também serão removidas.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Excluir") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancelar") }
        }
    )
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
