package com.caderninho.app.ui.screens.clientes

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
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.ui.components.CampoFormularioDialog
import com.caderninho.app.ui.components.CampoTexto
import com.caderninho.app.ui.components.SeloStatus
import com.caderninho.app.ui.theme.Vermelho
import com.caderninho.app.ui.theme.VerdeCaderninho
import com.caderninho.app.util.Formatadores

@Composable
fun ClientesScreen(
    onClienteClick: (Long) -> Unit,
    viewModel: ClientesViewModel = hiltViewModel()
) {
    val clientes by viewModel.clientes.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar cliente")
            }
        }
    ) { padding ->
        if (clientes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum cliente cadastrado ainda.\nToque em + para começar.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(clientes, key = { it.cliente.id }) { item ->
                    ClienteCard(
                        nome = item.cliente.nome,
                        telefone = item.cliente.telefone,
                        saldoPendente = item.saldoPendente,
                        onClick = { onClienteClick(item.cliente.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        CampoFormularioDialog(
            titulo = "Novo cliente",
            campos = listOf(
                CampoTexto(chave = "nome", rotulo = "Nome"),
                CampoTexto(chave = "telefone", rotulo = "Telefone (WhatsApp)")
            ),
            onConfirmar = { valores ->
                viewModel.adicionarCliente(valores["nome"].orEmpty(), valores["telefone"].orEmpty())
                mostrarDialogo = false
            },
            onCancelar = { mostrarDialogo = false }
        )
    }
}

@Composable
private fun ClienteCard(
    nome: String,
    telefone: String,
    saldoPendente: Double,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nome, style = MaterialTheme.typography.titleMedium)
            Text(telefone, style = MaterialTheme.typography.bodyMedium)
            if (saldoPendente > 0.0) {
                SeloStatus(
                    texto = "Fiado: ${Formatadores.moeda(saldoPendente)}",
                    corFundo = Vermelho,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                SeloStatus(
                    texto = "Em dia",
                    corFundo = VerdeCaderninho,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
