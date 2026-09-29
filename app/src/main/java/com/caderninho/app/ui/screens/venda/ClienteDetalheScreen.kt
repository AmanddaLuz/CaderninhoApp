package com.caderninho.app.ui.screens.venda

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.ui.components.SeloStatus
import com.caderninho.app.ui.theme.Ambar
import com.caderninho.app.ui.theme.VerdeCaderninho
import com.caderninho.app.util.Formatadores
import com.caderninho.app.util.WhatsAppUtil

@Composable
fun ClienteDetalheScreen(
    viewModel: ClienteDetalheViewModel = hiltViewModel()
) {
    val clienteComVendas by viewModel.clienteComVendas.collectAsState()
    val context = LocalContext.current
    var mostrarFormulario by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarFormulario = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nova venda")
            }
        }
    ) { padding ->
        val dados = clienteComVendas
        if (dados == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Carregando...")
            }
            return@Scaffold
        }

        val saldoPendente = dados.vendas.filter { it.status == StatusPagamento.PENDENTE }.sumOf { it.valor }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ClienteHeaderCard(
                nome = dados.cliente.nome,
                telefone = dados.cliente.telefone,
                saldoPendente = saldoPendente,
                onCobrar = {
                    val mensagem = "Olá ${dados.cliente.nome}! Passando para lembrar que " +
                        "você tem ${Formatadores.moeda(saldoPendente)} em aberto. " +
                        "Pode pagar quando puder, obrigado! 🙂"
                    WhatsAppUtil.enviarCobranca(
                        context = context,
                        telefone = dados.cliente.telefone,
                        mensagem = mensagem
                    )
                }
            )

            if (dados.vendas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma venda registrada ainda.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dados.vendas, key = { it.id }) { venda ->
                        VendaCard(
                            venda = venda,
                            onMarcarPago = { viewModel.marcarComoPago(venda) },
                            onMarcarPendente = { viewModel.marcarComoPendente(venda) },
                            onRemover = { viewModel.removerVenda(venda) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioVendaDialog(
            onConfirmar = { descricao, valor, forma, jaPago ->
                viewModel.registrarVenda(descricao, valor, forma, jaPago)
                mostrarFormulario = false
            },
            onCancelar = { mostrarFormulario = false }
        )
    }
}

@Composable
private fun ClienteHeaderCard(
    nome: String,
    telefone: String,
    saldoPendente: Double,
    onCobrar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nome, style = MaterialTheme.typography.titleLarge)
            Text(telefone, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Saldo pendente: ${Formatadores.moeda(saldoPendente)}")
                if (saldoPendente > 0.0) {
                    TextButton(onClick = onCobrar) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Cobrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun VendaCard(
    venda: VendaEntity,
    onMarcarPago: () -> Unit,
    onMarcarPendente: () -> Unit,
    onRemover: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(venda.descricao, style = MaterialTheme.typography.bodyLarge)
                Text(Formatadores.dataCurta(venda.criadoEm), style = MaterialTheme.typography.labelSmall)
                Text(Formatadores.moeda(venda.valor), style = MaterialTheme.typography.titleMedium)
                SeloStatus(
                    texto = if (venda.status == StatusPagamento.PAGO) "Pago" else "Pendente",
                    corFundo = if (venda.status == StatusPagamento.PAGO) VerdeCaderninho else Ambar,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Opções da venda")
                }
                DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                    if (venda.status == StatusPagamento.PENDENTE) {
                        DropdownMenuItem(text = { Text("Marcar como pago") }, onClick = {
                            onMarcarPago(); menuAberto = false
                        })
                    } else {
                        DropdownMenuItem(text = { Text("Marcar como pendente") }, onClick = {
                            onMarcarPendente(); menuAberto = false
                        })
                    }
                    DropdownMenuItem(text = { Text("Remover") }, onClick = {
                        onRemover(); menuAberto = false
                    })
                }
            }
        }
    }
}

@Composable
private fun FormularioVendaDialog(
    onConfirmar: (descricao: String, valor: Double, forma: FormaPagamento, jaPago: Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var valorTexto by remember { mutableStateOf("") }
    var formaSelecionada by remember { mutableStateOf(FormaPagamento.DINHEIRO) }
    var jaPago by remember { mutableStateOf(false) }
    var menuFormaAberto by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova venda ou serviço") },
        text = {
            Column {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Valor (R$)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                Box {
                    TextButton(onClick = { menuFormaAberto = true }) {
                        Text("Forma de pagamento: ${formaSelecionada.rotulo()}")
                    }
                    DropdownMenu(expanded = menuFormaAberto, onDismissRequest = { menuFormaAberto = false }) {
                        FormaPagamento.entries.forEach { forma ->
                            DropdownMenuItem(text = { Text(forma.rotulo()) }, onClick = {
                                formaSelecionada = forma
                                menuFormaAberto = false
                            })
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Já foi pago agora?")
                    androidx.compose.material3.Switch(checked = jaPago, onCheckedChange = { jaPago = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val valor = valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                onConfirmar(descricao, valor, formaSelecionada, jaPago)
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

private fun FormaPagamento.rotulo(): String = when (this) {
    FormaPagamento.DINHEIRO -> "Dinheiro"
    FormaPagamento.PIX -> "Pix"
    FormaPagamento.CARTAO_CREDITO -> "Cartão de crédito"
    FormaPagamento.CARTAO_DEBITO -> "Cartão de débito"
    FormaPagamento.OUTRO -> "Outro"
}
