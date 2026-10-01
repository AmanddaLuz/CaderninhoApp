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
    val estado by viewModel.uiState.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.limparErroCadastro()
                mostrarDialogo = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar cliente")
            }
        }
    ) { padding ->
        ConteudoClientes(
            estado = estado,
            onConsultaAlterada = viewModel::atualizarConsulta,
            onClienteClick = onClienteClick,
            modifier = Modifier.padding(padding)
        )
    }

    if (mostrarDialogo) {
        CampoFormularioDialog(
            titulo = "Novo cliente",
            campos = listOf(
                CampoTexto(chave = "nome", rotulo = "Nome"),
                CampoTexto(
                    chave = "telefone",
                    rotulo = "Telefone (WhatsApp)",
                    tipoTeclado = KeyboardType.Phone
                ),
                CampoTexto(
                    chave = "cpf",
                    rotulo = "CPF (opcional)",
                    tipoTeclado = KeyboardType.Number,
                    somenteDigitos = true,
                    tamanhoMaximo = 11
                )
            ),
            mensagemErro = estado.erroCadastro,
            onConfirmar = { valores ->
                val salvo = viewModel.adicionarCliente(
                    nome = valores["nome"].orEmpty(),
                    telefone = valores["telefone"].orEmpty(),
                    cpf = valores["cpf"].orEmpty()
                )
                if (salvo) mostrarDialogo = false
            },
            onCancelar = {
                viewModel.limparErroCadastro()
                mostrarDialogo = false
            }
        )
    }
}

@Composable
private fun ConteudoClientes(
    estado: ClientesUiState,
    onConsultaAlterada: (String) -> Unit,
    onClienteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = estado.consulta,
            onValueChange = onConsultaAlterada,
            label = { Text("Buscar por nome ou telefone") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (estado.clientes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val mensagem = if (estado.consulta.isBlank()) {
                    "Nenhum cliente cadastrado ainda.\nToque em + para começar."
                } else {
                    "Nenhum cliente encontrado."
                }
                Text(mensagem)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(estado.clientes, key = { it.cliente.id }) { item ->
                    ClienteCard(
                        nome = item.cliente.nome,
                        telefone = item.cliente.telefone,
                        saldoPendenteCentavos = item.saldoPendenteCentavos,
                        onClick = { onClienteClick(item.cliente.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClienteCard(
    nome: String,
    telefone: String,
    saldoPendenteCentavos: Long,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nome, style = MaterialTheme.typography.titleMedium)
            Text(telefone, style = MaterialTheme.typography.bodyMedium)
            if (saldoPendenteCentavos > 0L) {
                SeloStatus(
                    texto = "Fiado: ${Formatadores.moedaCentavos(saldoPendenteCentavos)}",
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
