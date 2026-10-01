package com.caderninho.app.ui.screens.resumo

import app.cash.turbine.test
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.fakes.FakeClienteDao
import com.caderninho.app.fakes.FakeVendaDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.VendaTeste
import com.caderninho.app.fakes.salvarVendaTeste
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneId

@ExperimentalCoroutinesApi
class ResumoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun instanteNoMesAtual(): Long =
        YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1

    private fun instanteNoMesPassado(): Long =
        YearMonth.now().minusMonths(1).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test
    fun `estado aggregates only sales from the current month`() = runTest {
        val vendaDao = FakeVendaDao()
        val clienteDao = FakeClienteDao(vendaDao)
        val repositorio = CaderninhoRepository(clienteDao, vendaDao)
        val clienteId = repositorio.salvarCliente(ClienteEntity(nome = "Carlos", telefone = "11966665555"))

        repositorio.salvarVendaTeste(
            clienteId = clienteId,
            descricao = "Venda deste mes paga",
            valorCentavos = 10_000,
            status = StatusPagamento.PAGO,
            dados = VendaTeste(
                formaPagamento = FormaPagamento.PIX,
                criadoEm = instanteNoMesAtual(),
                pagoEm = instanteNoMesAtual()
            )
        )
        repositorio.salvarVendaTeste(
            clienteId = clienteId,
            descricao = "Venda deste mes pendente",
            valorCentavos = 5_000,
            status = StatusPagamento.PENDENTE,
            dados = VendaTeste(criadoEm = instanteNoMesAtual())
        )
        repositorio.salvarVendaTeste(
            clienteId = clienteId,
            descricao = "Venda do mes passado",
            valorCentavos = 99_900,
            status = StatusPagamento.PAGO,
            dados = VendaTeste(
                formaPagamento = FormaPagamento.PIX,
                criadoEm = instanteNoMesPassado(),
                pagoEm = instanteNoMesPassado()
            )
        )

        val viewModel = ResumoViewModel(repositorio)

        viewModel.estado.test {
            skipItems(1)
            val estado = awaitItem()
            assertEquals(100.0, estado.totalRecebido, 0.0)
            assertEquals(50.0, estado.totalPendente, 0.0)
            assertEquals(2, estado.quantidadeVendas)
            assertEquals(100.0, estado.porFormaPagamento[FormaPagamento.PIX])
        }
    }
}
