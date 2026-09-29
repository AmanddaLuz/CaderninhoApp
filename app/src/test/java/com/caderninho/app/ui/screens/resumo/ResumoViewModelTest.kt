package com.caderninho.app.ui.screens.resumo

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

        repositorio.salvarVenda(
            VendaEntity(
                clienteId = clienteId,
                descricao = "Venda deste mes paga",
                valor = 100.0,
                formaPagamento = FormaPagamento.PIX,
                status = StatusPagamento.PAGO,
                criadoEm = instanteNoMesAtual(),
                pagoEm = instanteNoMesAtual()
            )
        )
        repositorio.salvarVenda(
            VendaEntity(
                clienteId = clienteId,
                descricao = "Venda deste mes pendente",
                valor = 50.0,
                formaPagamento = FormaPagamento.DINHEIRO,
                status = StatusPagamento.PENDENTE,
                criadoEm = instanteNoMesAtual()
            )
        )
        repositorio.salvarVenda(
            VendaEntity(
                clienteId = clienteId,
                descricao = "Venda do mes passado",
                valor = 999.0,
                formaPagamento = FormaPagamento.PIX,
                status = StatusPagamento.PAGO,
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
