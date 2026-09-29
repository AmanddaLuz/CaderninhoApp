package com.caderninho.app.ui.screens.venda

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.fakes.FakeClienteDao
import com.caderninho.app.fakes.FakeVendaDao
import com.caderninho.app.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClienteDetalheViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun criarViewModel(): Pair<ClienteDetalheViewModel, CaderninhoRepository> {
        val vendaDao = FakeVendaDao()
        val clienteDao = FakeClienteDao(vendaDao)
        val repositorio = CaderninhoRepository(clienteDao, vendaDao)
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Ana", telefone = "11977776666"))
        val viewModel = ClienteDetalheViewModel(repositorio, SavedStateHandle(mapOf("clienteId" to clienteId)))
        return viewModel to repositorio
    }

    @Test
    fun `registrarVenda ignores a non positive value`() = runTest {
        val (viewModel, _) = criarViewModel()

        viewModel.registrarVenda("Corte", 0.0, FormaPagamento.DINHEIRO, jaPago = false)

        viewModel.clienteComVendas.test {
            skipItems(1)
            assertTrue(awaitItem()?.vendas.orEmpty().isEmpty())
        }
    }

    @Test
    fun `registrarVenda marks an already paid sale with pagoEm`() = runTest {
        val (viewModel, _) = criarViewModel()

        viewModel.registrarVenda("Corte", 40.0, FormaPagamento.PIX, jaPago = true)

        viewModel.clienteComVendas.test {
            skipItems(1)
            val venda = awaitItem()?.vendas?.single()
            assertEquals(StatusPagamento.PAGO, venda?.status)
            assertNotNull(venda?.pagoEm)
        }
    }

    @Test
    fun `marcarComoPendente clears pagoEm`() = runTest {
        val (viewModel, _) = criarViewModel()
        viewModel.registrarVenda("Corte", 40.0, FormaPagamento.PIX, jaPago = true)

        viewModel.clienteComVendas.test {
            skipItems(1)
            val venda = awaitItem()!!.vendas.single()
            viewModel.marcarComoPendente(venda)

            val atualizado = awaitItem()!!.vendas.single()
            assertEquals(StatusPagamento.PENDENTE, atualizado.status)
            assertNull(atualizado.pagoEm)
        }
    }

    @Test
    fun `marcarComoPago sets pagoEm`() = runTest {
        val (viewModel, _) = criarViewModel()
        viewModel.registrarVenda("Corte", 40.0, FormaPagamento.PIX, jaPago = false)

        viewModel.clienteComVendas.test {
            skipItems(1)
            val venda = awaitItem()!!.vendas.single()
            viewModel.marcarComoPago(venda)

            val atualizado = awaitItem()!!.vendas.single()
            assertEquals(StatusPagamento.PAGO, atualizado.status)
            assertNotNull(atualizado.pagoEm)
        }
    }

    @Test
    fun `removerVenda deletes the sale`() = runTest {
        val (viewModel, _) = criarViewModel()
        viewModel.registrarVenda("Corte", 40.0, FormaPagamento.PIX, jaPago = false)

        viewModel.clienteComVendas.test {
            skipItems(1)
            val venda = awaitItem()!!.vendas.single()
            viewModel.removerVenda(venda)

            assertTrue(awaitItem()!!.vendas.isEmpty())
        }
    }
}
