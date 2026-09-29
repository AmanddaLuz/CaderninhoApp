package com.caderninho.app.ui.screens.clientes

import app.cash.turbine.test
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.fakes.FakeClienteDao
import com.caderninho.app.fakes.FakeVendaDao
import com.caderninho.app.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        viewModel.adicionarCliente(nome = "   ", telefone = "11999999999")

        viewModel.clientes.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `adicionarCliente persists a trimmed client`() = runTest {
        val repositorio = criarRepositorio()
        val viewModel = ClientesViewModel(repositorio)

        viewModel.adicionarCliente(nome = " Maria ", telefone = " 11999999999 ")

        viewModel.clientes.test {
            skipItems(1)
            val item = awaitItem().single()
            assertEquals("Maria", item.cliente.nome)
            assertEquals(0.0, item.saldoPendente, 0.0)
        }
    }

    @Test
    fun `saldoPendente sums only pending sales for a client`() = runTest {
        val repositorio = criarRepositorio()
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Joao", telefone = "11988887777"))
        repositorio.salvarVenda(
            VendaEntity(
                clienteId = clienteId,
                descricao = "Corte",
                valor = 30.0,
                formaPagamento = FormaPagamento.DINHEIRO,
                status = StatusPagamento.PENDENTE
            )
        )
        repositorio.salvarVenda(
            VendaEntity(
                clienteId = clienteId,
                descricao = "Manicure",
                valor = 20.0,
                formaPagamento = FormaPagamento.PIX,
                status = StatusPagamento.PAGO,
                pagoEm = System.currentTimeMillis()
            )
        )

        val viewModel = ClientesViewModel(repositorio)

        viewModel.clientes.test {
            skipItems(1)
            val item = awaitItem().single()
            assertEquals(30.0, item.saldoPendente, 0.0)
        }
    }

    @Test
    fun `removerCliente deletes the client`() = runTest {
        val repositorio = criarRepositorio()
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Joao", telefone = "11988887777"))
        val viewModel = ClientesViewModel(repositorio)

        viewModel.clientes.test {
            skipItems(1)
            val cliente = awaitItem().single().cliente
            viewModel.removerCliente(cliente)

            assertTrue(awaitItem().isEmpty())
        }
    }
}
