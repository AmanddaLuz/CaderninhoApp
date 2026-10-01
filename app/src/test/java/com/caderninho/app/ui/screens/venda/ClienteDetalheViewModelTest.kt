package com.caderninho.app.ui.screens.venda

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento
import com.caderninho.app.fakes.FakeClienteDao
import com.caderninho.app.fakes.FakeLembreteCobrancaScheduler
import com.caderninho.app.fakes.FakeVendaDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.VendaTeste
import com.caderninho.app.fakes.salvarVendaTeste
import com.caderninho.app.notification.LembreteCobrancaCoordinator
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClienteDetalheViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun criarContexto(): TestContext {
        val vendaDao = FakeVendaDao()
        val clienteDao = FakeClienteDao(vendaDao)
        val repositorio = CaderninhoRepository(clienteDao, vendaDao)
        val scheduler = FakeLembreteCobrancaScheduler()
        val clienteId = repositorio.salvarCliente(
            ClienteEntity(nome = "Ana", telefone = "11977776666")
        )
        val viewModel = ClienteDetalheViewModel(
            repository = repositorio,
            lembretes = LembreteCobrancaCoordinator(repositorio, scheduler),
            savedStateHandle = SavedStateHandle(
                mapOf("clienteId" to clienteId, "cobrar" to false)
            )
        )
        return TestContext(viewModel, repositorio, scheduler, clienteId)
    }

    @Test
    fun `registrarVenda rejects invalid items and pending sale without due date`() = runTest {
        val contexto = criarContexto()

        val itemInvalido = listOf(ItemVendaFormulario("", "0", ""))
        assertFalse(
            contexto.viewModel.registrarVenda(
                itemInvalido,
                FormaPagamento.DINHEIRO,
                jaPago = false,
                vencimentoEpochDay = null
            )
        )
        assertEquals(
            "Preencha descrição, quantidade e valor de todos os itens.",
            contexto.viewModel.erroFormulario.value
        )

        assertFalse(
            contexto.viewModel.registrarVenda(
                itemValido(),
                FormaPagamento.DINHEIRO,
                jaPago = false,
                vencimentoEpochDay = null
            )
        )
        assertEquals(
            "Informe a data prevista de pagamento.",
            contexto.viewModel.erroFormulario.value
        )
    }

    @Test
    fun `registrarVenda calculates item totals and schedules pending group`() = runTest {
        val contexto = criarContexto()
        val vencimento = LocalDate.now().plusDays(2).toEpochDay()

        contexto.viewModel.clienteComVendas.test {
            awaitItem()
            assertTrue(
                contexto.viewModel.registrarVenda(
                    itensFormulario = listOf(
                        ItemVendaFormulario("Arroz", "2", "12,50"),
                        ItemVendaFormulario("Queijo", "0,5", "40,00")
                    ),
                    forma = FormaPagamento.PIX,
                    jaPago = false,
                    vencimentoEpochDay = vencimento
                )
            )

            val venda = awaitItem()!!.vendas.single()
            assertEquals(4_500L, venda.totalCentavos)
            assertEquals(2, venda.itens.size)
            assertEquals(vencimento, venda.venda.vencimentoEpochDay)
            assertEquals(listOf(contexto.clienteId to vencimento), contexto.scheduler.agendados)
        }
    }

    @Test
    fun `paid sale stamps pagoEm and does not schedule a reminder`() = runTest {
        val contexto = criarContexto()

        contexto.viewModel.clienteComVendas.test {
            awaitItem()
            assertTrue(
                contexto.viewModel.registrarVenda(
                    itemValido(),
                    FormaPagamento.PIX,
                    jaPago = true,
                    vencimentoEpochDay = null
                )
            )

            val venda = awaitItem()!!.vendas.single().venda
            assertEquals(StatusPagamento.PAGO, venda.status)
            assertNotNull(venda.pagoEm)
            assertTrue(contexto.scheduler.agendados.isEmpty())
        }
    }

    @Test
    fun `marking the only pending sale paid cancels its grouped reminder`() = runTest {
        val contexto = criarContexto()
        val vencimento = LocalDate.now().plusDays(1).toEpochDay()

        contexto.viewModel.clienteComVendas.test {
            awaitItem()
            contexto.viewModel.registrarVenda(
                itemValido(),
                FormaPagamento.PIX,
                jaPago = false,
                vencimentoEpochDay = vencimento
            )
            val venda = awaitItem()!!.vendas.single().venda

            contexto.viewModel.marcarComoPago(venda)
            val atualizada = awaitItem()!!.vendas.single().venda
            assertEquals(StatusPagamento.PAGO, atualizada.status)
            assertNotNull(atualizada.pagoEm)
            assertEquals(vencimento, atualizada.vencimentoEpochDay)
            assertEquals(listOf(contexto.clienteId to vencimento), contexto.scheduler.cancelados)
        }
    }

    @Test
    fun `charge preselects overdue and today sales and totals only selected sales`() = runTest {
        val contexto = criarContexto()
        val hoje = LocalDate.now().toEpochDay()
        contexto.repository.salvarVendaTeste(
            contexto.clienteId,
            "Atrasada",
            1_000,
            StatusPagamento.PENDENTE,
            VendaTeste(vencimentoEpochDay = hoje - 1)
        )
        contexto.repository.salvarVendaTeste(
            contexto.clienteId,
            "Hoje",
            2_000,
            StatusPagamento.PENDENTE,
            VendaTeste(vencimentoEpochDay = hoje)
        )
        contexto.repository.salvarVendaTeste(
            contexto.clienteId,
            "Futura",
            3_000,
            StatusPagamento.PENDENTE,
            VendaTeste(vencimentoEpochDay = hoje + 1)
        )

        contexto.viewModel.cobranca.test {
            awaitItem()
            contexto.viewModel.iniciarCobranca()
            val inicial = awaitItem().let { estado ->
                if (estado.aberta) estado else awaitItem()
            }

            @Test
            fun `confirm charge emits message with selected sales only`() = runTest {
                val contexto = criarContexto()
                val hoje = LocalDate.now().toEpochDay()
                contexto.repository.salvarVendaTeste(
                    contexto.clienteId,
                    "Selecionada",
                    1_500,
                    StatusPagamento.PENDENTE,
                    VendaTeste(vencimentoEpochDay = hoje)
                )
                contexto.repository.salvarVendaTeste(
                    contexto.clienteId,
                    "Não selecionada",
                    4_000,
                    StatusPagamento.PENDENTE,
                    VendaTeste(vencimentoEpochDay = hoje + 1)
                )

                contexto.viewModel.cobranca.test {
                    awaitItem()
                    contexto.viewModel.iniciarCobranca()
                    val aberta = awaitItem().let { if (it.aberta) it else awaitItem() }
                    assertEquals(1_500L, aberta.totalSelecionadoCentavos)

                    val envio = async { contexto.viewModel.enviosCobranca.first() }
                    contexto.viewModel.confirmarCobranca()
                    val mensagem = envio.await()
                    assertTrue(mensagem.mensagem.contains("Selecionada"))
                    assertFalse(mensagem.mensagem.contains("Não selecionada"))
                    assertTrue(mensagem.mensagem.contains("R$"))
                }
            }
            assertEquals(2, inicial.vendas.count(VendaCobrancaUiModel::selecionada))
            assertEquals(3_000L, inicial.totalSelecionadoCentavos)

            contexto.viewModel.selecionarTodasCobrancas(true)
            val todas = awaitItem()
            assertTrue(todas.todasSelecionadas)
            assertEquals(6_000L, todas.totalSelecionadoCentavos)

            val futura = todas.vendas.single {
                it.venda.venda.vencimentoEpochDay == hoje + 1
            }
            contexto.viewModel.alternarVendaCobranca(futura.venda.venda.id)
            val ajustada = awaitItem()
            assertEquals(3_000L, ajustada.totalSelecionadoCentavos)
        }
    }

    @Test
    fun `marking sale pending requires a non past date and clears pagoEm`() = runTest {
        val contexto = criarContexto()
        contexto.repository.salvarVendaTeste(
            contexto.clienteId,
            "Paga",
            2_000,
            StatusPagamento.PAGO,
            VendaTeste(pagoEm = System.currentTimeMillis())
        )

        contexto.viewModel.clienteComVendas.test {
            val primeiro = awaitItem()
            val venda = (primeiro ?: awaitItem())!!.vendas.single().venda
            assertFalse(
                contexto.viewModel.marcarComoPendente(
                    venda,
                    LocalDate.now().minusDays(1).toEpochDay()
                )
            )
            val vencimento = LocalDate.now().plusDays(1).toEpochDay()
            assertTrue(contexto.viewModel.marcarComoPendente(venda, vencimento))

            val atualizada = awaitItem()!!.vendas.single().venda
            assertEquals(StatusPagamento.PENDENTE, atualizada.status)
            assertNull(atualizada.pagoEm)
            assertEquals(vencimento, atualizada.vencimentoEpochDay)
        }
    }

    private fun itemValido() =
        listOf(ItemVendaFormulario("Corte", "1", "40,00"))

    private data class TestContext(
        val viewModel: ClienteDetalheViewModel,
        val repository: CaderninhoRepository,
        val scheduler: FakeLembreteCobrancaScheduler,
        val clienteId: Long
    )
}
