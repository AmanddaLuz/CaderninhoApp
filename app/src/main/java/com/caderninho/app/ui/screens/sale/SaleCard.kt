package com.caderninho.app.ui.screens.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.ui.components.StatusBadge
import com.caderninho.app.ui.theme.SuccessGreen
import com.caderninho.app.ui.theme.WarningAmber
import com.caderninho.app.util.Formatters
import java.time.LocalDate

private data class SaleMenuActions(
    val markPaid: () -> Unit,
    val markPending: () -> Unit,
    val reschedule: () -> Unit,
    val delete: () -> Unit
)

@Composable
internal fun SaleCard(
    sale: SaleWithItems,
    onMarkPaid: () -> Unit,
    onMarkPending: () -> Unit,
    onReschedule: () -> Unit,
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
                SaleActionsMenu(
                    expanded = isMenuOpen,
                    status = sale.sale.status,
                    onDismiss = { isMenuOpen = false },
                    actions = SaleMenuActions(
                        markPaid = onMarkPaid,
                        markPending = onMarkPending,
                        reschedule = onReschedule,
                        delete = onDelete
                    )
                )
            }
        }
        if (showDetails) {
            SaleDetailsDialog(sale = sale, onClose = { showDetails = false })
        }
    }
}

@Composable
private fun SaleActionsMenu(
    expanded: Boolean,
    status: PaymentStatus,
    onDismiss: () -> Unit,
    actions: SaleMenuActions
) {
    val isPending = status == PaymentStatus.PENDING
    val paymentLabel = if (isPending) "Marcar como pago" else "Marcar como pendente"
    val paymentAction = if (isPending) actions.markPaid else actions.markPending

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (isPending) {
            SaleMenuItem("Remarcar data", actions.reschedule, onDismiss)
        }
        SaleMenuItem(paymentLabel, paymentAction, onDismiss)
        SaleMenuItem("Remover", actions.delete, onDismiss)
    }
}

@Composable
private fun SaleMenuItem(
    text: String,
    action: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = {
            action()
            onDismiss()
        }
    )
}

@Composable
private fun SaleCardContent(
    sale: SaleWithItems,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(Formatters.shortDate(sale.sale.createdAt), style = MaterialTheme.typography.labelSmall)
        sale.sale.dueEpochDay?.let {
            Text("Previsto: ${LocalDate.ofEpochDay(it).formatDate()}")
        }
        Text(Formatters.currencyFromCents(sale.totalCents))
        StatusBadge(
            text = if (sale.sale.status == PaymentStatus.PAID) "Pago" else "Pendente",
            backgroundColor = if (sale.sale.status == PaymentStatus.PAID) {
                SuccessGreen
            } else {
                WarningAmber
            }
        )
    }
}
