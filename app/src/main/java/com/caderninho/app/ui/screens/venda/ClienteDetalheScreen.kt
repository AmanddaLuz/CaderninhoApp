package com.caderninho.app.ui.screens.venda

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
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.ui.components.SeloStatus
import com.caderninho.app.ui.theme.Ambar
import com.caderninho.app.ui.theme.VerdeCaderninho
import com.caderninho.app.util.Formatadores
import com.caderninho.app.util.WhatsAppUtil
import java.time.LocalDate

private data class ClienteDetalheAcoes(
    val cobrar: () -> Unit,
    val marcarPago: (VendaEntity) -> Unit,
    val marcarPendente: (VendaEntity) -> Unit,
    val remover: (VendaEntity) -> Unit
)

@Composable
fun ClienteDetalheScreen(
    viewModel: ClienteDetalheViewModel = hiltViewModel()
) {
    val dados by viewModel.clienteComVendas.collectAsState()
    val cobranca by viewModel.cobranca.collectAsState()
    val erroFormulario by viewModel.erroFormulario.collectAsState()
    val context = LocalContext.current
    var mostrarFormulario by remember { mutableStateOf(false) }
    var vendaParaPendente by remember { mutableStateOf<VendaEntity?>(null) }
    val solicitarNotificacao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    ClienteDetalheEfeitos(viewModel, context)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.limparErroFormulario()
                mostrarFormulario = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Nova venda")
            }
        }
    ) { padding ->
        ClienteDetalheConteudo(
            dados = dados,
            acoes = ClienteDetalheAcoes(
                cobrar = viewModel::iniciarCobranca,
                marcarPago = viewModel::marcarComoPago,
                marcarPendente = { vendaParaPendente = it },
                remover = viewModel::removerVenda
            ),
            modifier = Modifier.padding(padding)
        )
    }

    if (mostrarFormulario) {
        FormularioVendaDialog(
            mensagemErro = erroFormulario,
            onConfirmar = { itens, forma, jaPago, vencimento ->
                val salvo = viewModel.registrarVenda(itens, forma, jaPago, vencimento)
                if (salvo) {
                    solicitarPermissaoNotificacao(context, solicitarNotificacao, jaPago)
                    mostrarFormulario = false
                }
            },
            onCancelar = {
                viewModel.limparErroFormulario()
                mostrarFormulario = false
            }
        )
    }
    if (cobranca.aberta) {
        CobrancaDialog(
            estado = cobranca,
            onDefinirVenda = viewModel::definirVendaCobranca,
            onSelecionarTodas = viewModel::selecionarTodasCobrancas,
            onConfirmar = viewModel::confirmarCobranca,
            onCancelar = viewModel::fecharCobranca
        )
    }
    vendaParaPendente?.let { venda ->
        SeletorDataDialog(
            titulo = "Data prevista de pagamento",
            dataInicialEpochDay = LocalDate.now().toEpochDay(),
            onConfirmar = { data ->
                if (viewModel.marcarComoPendente(venda, data)) vendaParaPendente = null
            },
            onCancelar = { vendaParaPendente = null }
        )
    }
}

@Composable
private fun ClienteDetalheEfeitos(viewModel: ClienteDetalheViewModel, context: Context) {
    LaunchedEffect(viewModel) {
        viewModel.enviosCobranca.collect { envio ->
            WhatsAppUtil.enviarCobranca(context, envio.telefone, envio.mensagem)
        }
    }
    LaunchedEffect(viewModel.abrirCobrancaInicial) {
        if (viewModel.abrirCobrancaInicial) viewModel.iniciarCobranca()
    }
}

@Composable
private fun ClienteDetalheConteudo(
    dados: ClienteComVendas?,
    acoes: ClienteDetalheAcoes,
    modifier: Modifier = Modifier
) {
    if (dados == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregando...")
        }
        return
    }
    val saldoPendente = dados.vendas
        .filter { it.venda.status == StatusPagamento.PENDENTE }
        .sumOf { it.totalCentavos }
    Column(modifier = modifier.fillMaxSize()) {
        ClienteHeaderCard(
            nome = dados.cliente.nome,
            telefone = dados.cliente.telefone,
            saldoPendenteCentavos = saldoPendente,
            onCobrar = acoes.cobrar
        )
        ListaVendas(
            vendas = dados.vendas,
            onMarcarPago = acoes.marcarPago,
            onMarcarPendente = acoes.marcarPendente,
            onRemover = acoes.remover,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ListaVendas(
    vendas: List<VendaComItens>,
    onMarcarPago: (VendaEntity) -> Unit,
    onMarcarPendente: (VendaEntity) -> Unit,
    onRemover: (VendaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (vendas.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Nenhuma venda registrada ainda.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            items(vendas, key = { it.venda.id }) { venda ->
                VendaCard(
                    venda = venda,
                    onMarcarPago = { onMarcarPago(venda.venda) },
                    onMarcarPendente = { onMarcarPendente(venda.venda) },
                    onRemover = { onRemover(venda.venda) }
                )
            }
        }
    }
}

@Composable
private fun ClienteHeaderCard(
    nome: String,
    telefone: String,
    saldoPendenteCentavos: Long,
    onCobrar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nome, style = MaterialTheme.typography.titleLarge)
            Text(telefone, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saldo pendente: ${Formatadores.moedaCentavos(saldoPendenteCentavos)}")
                if (saldoPendenteCentavos > 0L) {
                    TextButton(onClick = onCobrar) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Text("Cobrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun VendaCard(
    venda: VendaComItens,
    onMarcarPago: () -> Unit,
    onMarcarPendente: () -> Unit,
    onRemover: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    var mostrarDetalhes by remember { mutableStateOf(false) }
    Card(
        onClick = { mostrarDetalhes = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VendaCardConteudo(venda, Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuAberto = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opções da venda")
                }
                DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                    val pendente = venda.venda.status == StatusPagamento.PENDENTE
                    DropdownMenuItem(
                        text = { Text(if (pendente) "Marcar como pago" else "Marcar como pendente") },
                        onClick = {
                            if (pendente) onMarcarPago() else onMarcarPendente()
                            menuAberto = false
                        }
                    )
                    DropdownMenuItem(text = { Text("Remover") }, onClick = {
                        onRemover()
                        menuAberto = false
                    })
                }
            }
        }
        if (mostrarDetalhes) {
            DetalhesVendaDialog(venda = venda, onFechar = { mostrarDetalhes = false })
        }
    }
}

@Composable
private fun VendaCardConteudo(venda: VendaComItens, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(Formatadores.dataCurta(venda.venda.criadoEm), style = MaterialTheme.typography.labelSmall)
        venda.venda.vencimentoEpochDay?.let {
            Text("Previsto: ${LocalDate.ofEpochDay(it).formatarData()}")
        }
        Text(Formatadores.moedaCentavos(venda.totalCentavos))
        SeloStatus(
            texto = if (venda.venda.status == StatusPagamento.PAGO) "Pago" else "Pendente",
            corFundo = if (venda.venda.status == StatusPagamento.PAGO) VerdeCaderninho else Ambar
        )
    }
}

private fun solicitarPermissaoNotificacao(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    jaPago: Boolean
) {
    val precisaPermissao = !jaPago &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    if (precisaPermissao) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
}
