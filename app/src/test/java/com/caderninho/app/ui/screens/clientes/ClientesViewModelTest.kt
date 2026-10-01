package com.caderninho.app.ui.screens.clientes

import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.fakes.FakeClienteDao
import com.caderninho.app.fakes.FakeVendaDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.VendaTeste
import com.caderninho.app.fakes.salvarVendaTeste
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClientesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun criarRepositorio(): CaderninhoRepository {
        val vendaDao = FakeVendaDao()
        val clienteDao = FakeClienteDao(vendaDao)
        return CaderninhoRepository(clienteDao, vendaDao)
    }

    @Test
    fun `adicionarCliente ignores a blank name`() = runTest {
        val repositorio = criarRepositorio()
        val viewModel = ClientesViewModel(repositorio)

        val salvo = viewModel.adicionarCliente(nome = "   ", telefone = "11999999999", cpf = "")
        advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado.clientes.isEmpty())
        assertTrue(!salvo)
        assertEquals("Informe o nome do cliente.", estado.erroCadastro)
    }

    @Test
    fun `adicionarCliente persists a trimmed client`() = runTest {
        val repositorio = criarRepositorio()
        val viewModel = ClientesViewModel(repositorio)

        viewModel.adicionarCliente(
            nome = " Maria ",
            telefone = " 11999999999 ",
            cpf = "529.982.247-25"
        )

        advanceUntilIdle()
        val item = viewModel.uiState.value.clientes.single()
        assertEquals("Maria", item.cliente.nome)
        assertEquals("52998224725", item.cliente.cpf)
        assertEquals(0L, item.saldoPendenteCentavos)
    }

    @Test
    fun `saldoPendente sums only pending sales for a client`() = runTest {
        val repositorio = criarRepositorio()
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Joao", telefone = "11988887777"))
        repositorio.salvarVendaTeste(
            clienteId = clienteId,
            descricao = "Corte",
            valorCentavos = 3_000,
            status = StatusPagamento.PENDENTE
        )
        repositorio.salvarVendaTeste(
            clienteId = clienteId,
            descricao = "Manicure",
            valorCentavos = 2_000,
            status = StatusPagamento.PAGO,
            dados = VendaTeste(pagoEm = System.currentTimeMillis())
        )

        val viewModel = ClientesViewModel(repositorio)
        advanceUntilIdle()

        val item = viewModel.uiState.value.clientes.single()
        assertEquals(3_000L, item.saldoPendenteCentavos)
    }

    @Test
    fun `removerCliente deletes the client`() = runTest {
        val repositorio = criarRepositorio()
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Joao", telefone = "11988887777"))
        val viewModel = ClientesViewModel(repositorio)
        advanceUntilIdle()

        val cliente = viewModel.uiState.value.clientes.single().cliente
        viewModel.removerCliente(cliente)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.clientes.isEmpty())
    }

    @Test
    fun `atualizarConsulta filters by normalized name or phone digits`() = runTest {
        val repositorio = criarRepositorio()
        repositorio.salvarCliente(ClienteEntity(nome = "José Silva", telefone = "(11) 98888-7777"))
        repositorio.salvarCliente(ClienteEntity(nome = "Maria", telefone = "(21) 97777-6666"))
        val viewModel = ClientesViewModel(repositorio)
        advanceUntilIdle()

        viewModel.atualizarConsulta("jose")
        advanceUntilIdle()
        assertEquals(listOf("José Silva"), viewModel.uiState.value.clientes.map { it.cliente.nome })

        viewModel.atualizarConsulta("2197777")
        advanceUntilIdle()
        assertEquals(listOf("Maria"), viewModel.uiState.value.clientes.map { it.cliente.nome })
    }

    @Test
    fun `adicionarCliente rejects invalid and duplicate CPF`() = runTest {
        val repositorio = criarRepositorio()
        val viewModel = ClientesViewModel(repositorio)

        assertTrue(!viewModel.adicionarCliente("Ana", "11999999999", "11111111111"))
        advanceUntilIdle()
        assertEquals(
            "Informe um CPF válido ou deixe o campo vazio.",
            viewModel.uiState.value.erroCadastro
        )

        assertTrue(viewModel.adicionarCliente("Ana", "11999999999", "52998224725"))
        advanceUntilIdle()
        assertTrue(!viewModel.adicionarCliente("Outra Ana", "11888888888", "52998224725"))
        advanceUntilIdle()
        assertEquals("Este CPF já está cadastrado.", viewModel.uiState.value.erroCadastro)
    }
}
