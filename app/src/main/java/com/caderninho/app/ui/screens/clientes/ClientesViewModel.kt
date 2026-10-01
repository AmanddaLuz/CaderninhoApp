package com.caderninho.app.ui.screens.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.util.ValidadorCpf
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Normalizer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saldo pendente (fiado) de um cliente, calculado a partir das vendas em aberto. */
data class ClienteUiModel(
    val cliente: ClienteEntity,
    val saldoPendenteCentavos: Long
)

data class ClientesUiState(
    val clientes: List<ClienteUiModel> = emptyList(),
    val consulta: String = "",
    val erroCadastro: String? = null
)

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val repository: CaderninhoRepository
) : ViewModel() {

    private val todosClientes = repository.observarClientesComVendas()
        .map { lista -> lista.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val consulta = MutableStateFlow("")
    private val erroCadastro = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ClientesUiState> =
        combine(todosClientes, consulta, erroCadastro) { clientes, termo, erro ->
            ClientesUiState(
                clientes = clientes.filter { it.correspondeA(termo) },
                consulta = termo,
                erroCadastro = erro
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ClientesUiState()
        )

    fun atualizarConsulta(novaConsulta: String) {
        consulta.value = novaConsulta
    }

    fun adicionarCliente(
        nome: String,
        telefone: String,
        cpf: String,
        observacao: String = ""
    ): Boolean {
        val cpfNormalizado = ValidadorCpf.normalizar(cpf).ifBlank { null }
        erroCadastro.value = validarCadastro(nome, cpfNormalizado)
        if (erroCadastro.value != null) {
            return false
        }

        viewModelScope.launch {
            repository.salvarCliente(
                ClienteEntity(
                    nome = nome.trim(),
                    telefone = telefone.trim(),
                    cpf = cpfNormalizado,
                    observacao = observacao.trim()
                )
            )
        }
        return true
    }

    private fun validarCadastro(nome: String, cpf: String?): String? = when {
        nome.isBlank() -> "Informe o nome do cliente."
        cpf != null && !ValidadorCpf.validar(cpf) ->
            "Informe um CPF válido ou deixe o campo vazio."
        cpf != null && todosClientes.value.any { it.cliente.cpf == cpf } ->
            "Este CPF já está cadastrado."
        else -> null
    }

    fun limparErroCadastro() {
        erroCadastro.value = null
    }

    fun removerCliente(cliente: ClienteEntity) {
        viewModelScope.launch { repository.removerCliente(cliente) }
    }

    private fun ClienteComVendas.toUiModel(): ClienteUiModel {
        val saldo = vendas
            .filter { it.venda.status == StatusPagamento.PENDENTE }
            .sumOf { it.totalCentavos }
        return ClienteUiModel(cliente = cliente, saldoPendenteCentavos = saldo)
    }

    private fun ClienteUiModel.correspondeA(termo: String): Boolean {
        if (termo.isBlank()) return true
        val termoTexto = termo.normalizarTexto()
        val termoNumerico = termo.filter(Char::isDigit)
        return cliente.nome.normalizarTexto().contains(termoTexto) ||
            (termoNumerico.isNotEmpty() && cliente.telefone.filter(Char::isDigit).contains(termoNumerico))
    }

    private fun String.normalizarTexto(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(ACENTOS_REGEX, "")
            .lowercase()

    private companion object {
        val ACENTOS_REGEX = "\\p{M}+".toRegex()
    }
}
