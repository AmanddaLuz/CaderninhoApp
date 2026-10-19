package com.caderninho.app.ui.screens.clients

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.ui.components.EditClientDialog
import com.caderninho.app.ui.components.FormFieldsDialog
import com.caderninho.app.ui.components.NotebookFilterChip
import com.caderninho.app.ui.components.PhoneVisualTransformation
import com.caderninho.app.ui.components.TextFieldConfig
import com.caderninho.app.ui.components.StatusBadge
import com.caderninho.app.ui.navigation.ClientsFilter
import com.caderninho.app.ui.theme.ErrorRed
import com.caderninho.app.ui.theme.SuccessGreen
import com.caderninho.app.util.Formatters

private data class ClientsActions(
    val query: (String) -> Unit,
    val filter: (ClientsFilter) -> Unit,
    val click: (Long) -> Unit,
    val edit: (ClientEntity) -> Unit
)

private class ClientsDialogState {
    var showAddDialog by mutableStateOf(false)
    var clientBeingEdited by mutableStateOf<ClientEntity?>(null)
}

@Composable
fun ClientsScreen(
    onClientClick: (Long) -> Unit,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dialogState = remember { ClientsDialogState() }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.clearRegistrationError()
                dialogState.showAddDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar cliente")
            }
        }
    ) { padding ->
        ClientsContent(
            state = state,
            actions = ClientsActions(
                query = viewModel::updateQuery,
                filter = viewModel::updateFilter,
                click = onClientClick,
                edit = { client ->
                    viewModel.clientEditor.clearError()
                    dialogState.clientBeingEdited = client
                }
            ),
            modifier = Modifier.padding(padding)
        )
    }

    ClientsDialogs(viewModel = viewModel, state = state, dialogState = dialogState)
}

@Composable
private fun ClientsDialogs(
    viewModel: ClientsViewModel,
    state: ClientsUiState,
    dialogState: ClientsDialogState
) {
    val editError by viewModel.clientEditor.formError.collectAsState()

    if (dialogState.showAddDialog) {
        FormFieldsDialog(
            title = "Novo cliente",
            fields = listOf(
                TextFieldConfig(key = "name", label = "Nome"),
                TextFieldConfig(
                    key = "phone",
                    label = "Telefone (WhatsApp)",
                    keyboardType = KeyboardType.Phone,
                    digitsOnly = true,
                    maxLength = 11,
                    visualTransformation = PhoneVisualTransformation
                ),
                TextFieldConfig(
                    key = "cpf",
                    label = "CPF (opcional)",
                    keyboardType = KeyboardType.Number,
                    digitsOnly = true,
                    maxLength = 11
                )
            ),
            errorMessage = state.registrationError,
            onConfirm = { values ->
                val saved = viewModel.addClient(
                    name = values["name"].orEmpty(),
                    phone = values["phone"].orEmpty(),
                    cpf = values["cpf"].orEmpty()
                )
                if (saved) dialogState.showAddDialog = false
            },
            onCancel = {
                viewModel.clearRegistrationError()
                dialogState.showAddDialog = false
            }
        )
    }

    dialogState.clientBeingEdited?.let { client ->
        EditClientDialog(
            client = client,
            errorMessage = editError,
            onConfirm = { name, phone, cpf ->
                if (viewModel.clientEditor.update(client, name, phone, cpf)) {
                    dialogState.clientBeingEdited = null
                }
            },
            onCancel = {
                viewModel.clientEditor.clearError()
                dialogState.clientBeingEdited = null
            }
        )
    }
}

@Composable
private fun ClientsContent(
    state: ClientsUiState,
    actions: ClientsActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = actions.query,
            label = { Text("Buscar por nome ou telefone") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ClientsFilter.entries.forEach { option ->
                NotebookFilterChip(
                    selected = state.filter == option,
                    onClick = { actions.filter(option) },
                    label = option.label()
                )
            }
        }

        if (state.clients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val message = when {
                    state.query.isNotBlank() -> "Nenhum cliente encontrado."
                    state.filter == ClientsFilter.PENDING -> "Nenhum cliente com pendência."
                    state.filter == ClientsFilter.OVERDUE -> "Nenhum cliente atrasado."
                    else -> "Nenhum cliente cadastrado ainda.\nToque em + para começar."
                }
                Text(message)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(state.clients, key = { it.client.id }) { item ->
                    ClientCard(
                        name = item.client.name,
                        phone = item.client.phone,
                        pendingBalanceCents = item.pendingBalanceCents,
                        onClick = { actions.click(item.client.id) },
                        onEdit = { actions.edit(item.client) }
                    )
                }
            }
        }
    }
}

private fun ClientsFilter.label(): String = when (this) {
    ClientsFilter.ALL -> "Todos"
    ClientsFilter.PENDING -> "Pendências"
    ClientsFilter.OVERDUE -> "Atrasados"
}

@Composable
private fun ClientCard(
    name: String,
    phone: String,
    pendingBalanceCents: Long,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar cliente")
                }
            }
            Text(Formatters.phone(phone), style = MaterialTheme.typography.bodyMedium)
            if (pendingBalanceCents > 0L) {
                StatusBadge(
                    text = "Fiado: ${Formatters.currencyFromCents(pendingBalanceCents)}",
                    backgroundColor = ErrorRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                StatusBadge(
                    text = "Em dia",
                    backgroundColor = SuccessGreen,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
