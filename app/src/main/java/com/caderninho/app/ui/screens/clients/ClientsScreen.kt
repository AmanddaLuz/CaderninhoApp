package com.caderninho.app.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.caderninho.app.ui.components.FormFieldsDialog
import com.caderninho.app.ui.components.TextFieldConfig
import com.caderninho.app.ui.components.StatusBadge
import com.caderninho.app.ui.theme.Red
import com.caderninho.app.ui.theme.LedgerGreen
import com.caderninho.app.util.Formatters

@Composable
fun ClientsScreen(
    onClientClick: (Long) -> Unit,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.clearRegistrationError()
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar cliente")
            }
        }
    ) { padding ->
        ClientsContent(
            state = state,
            onQueryChanged = viewModel::updateQuery,
            onClientClick = onClientClick,
            modifier = Modifier.padding(padding)
        )
    }

    if (showDialog) {
        FormFieldsDialog(
            title = "Novo cliente",
            fields = listOf(
                TextFieldConfig(key = "name", label = "Nome"),
                TextFieldConfig(
                    key = "phone",
                    label = "Telefone (WhatsApp)",
                    keyboardType = KeyboardType.Phone
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
                if (saved) showDialog = false
            },
            onCancel = {
                viewModel.clearRegistrationError()
                showDialog = false
            }
        )
    }
}

@Composable
private fun ClientsContent(
    state: ClientsUiState,
    onQueryChanged: (String) -> Unit,
    onClientClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            label = { Text("Buscar por nome ou telefone") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state.clients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val message = if (state.query.isBlank()) {
                    "Nenhum cliente cadastrado ainda.\nToque em + para começar."
                } else {
                    "Nenhum cliente encontrado."
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
                        onClick = { onClientClick(item.client.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    name: String,
    phone: String,
    pendingBalanceCents: Long,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(phone, style = MaterialTheme.typography.bodyMedium)
            if (pendingBalanceCents > 0L) {
                StatusBadge(
                    text = "Fiado: ${Formatters.currencyFromCents(pendingBalanceCents)}",
                    backgroundColor = Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                StatusBadge(
                    text = "Em dia",
                    backgroundColor = LedgerGreen,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
