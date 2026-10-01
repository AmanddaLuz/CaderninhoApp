package com.caderninho.app.ui.screens.sale

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.fakes.FakeClientDao
import com.caderninho.app.fakes.FakeChargeReminderScheduler
import com.caderninho.app.fakes.FakeSaleDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.SaleTestData
import com.caderninho.app.fakes.saveTestSale
import com.caderninho.app.notification.ChargeReminderCoordinator
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
class ClientDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private suspend fun createContext(): TestContext {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        val repository = LedgerRepository(clientDao, saleDao)
        val scheduler = FakeChargeReminderScheduler()
        val clientId = repository.saveClient(
            ClientEntity(name = "Ana", phone = "11977776666")
        )
        val viewModel = ClientDetailViewModel(
            repository = repository,
            reminderCoordinator = ChargeReminderCoordinator(repository, scheduler),
            savedStateHandle = SavedStateHandle(
                mapOf("clientId" to clientId, "charge" to false)
            )
        )
        return TestContext(viewModel, repository, scheduler, clientId)
    }

    @Test
    fun `registerSale rejects invalid items and pending sale without due date`() = runTest {
        val context = createContext()

        val itemInvalido = listOf(SaleItemForm("", "0", ""))
        assertFalse(
            context.viewModel.registerSale(
                itemInvalido,
                PaymentMethod.CASH,
                isPaid = false,
                dueEpochDay = null
            )
        )
        assertEquals(
            "Preencha descrição, quantidade e valor de todos os itens.",
            context.viewModel.formError.value
        )

        assertFalse(
            context.viewModel.registerSale(
                itemValido(),
                PaymentMethod.CASH,
                isPaid = false,
                dueEpochDay = null
            )
        )
        assertEquals(
            "Informe a data prevista de pagamento.",
            context.viewModel.formError.value
        )
    }

    @Test
    fun `registerSale calculates item totals and schedules pending group`() = runTest {
        val context = createContext()
        val dueDate = LocalDate.now().plusDays(2).toEpochDay()

        context.viewModel.clientWithSales.test {
            awaitItem()
            assertTrue(
                context.viewModel.registerSale(
                    formItems = listOf(
                        SaleItemForm("Arroz", "2", "12,50"),
                        SaleItemForm("Queijo", "0,5", "40,00")
                    ),
                    method = PaymentMethod.PIX,
                    isPaid = false,
                    dueEpochDay = dueDate
                )
            )

            val sale = awaitItem()!!.sales.single()
            assertEquals(4_500L, sale.totalCents)
            assertEquals(2, sale.items.size)
            assertEquals(dueDate, sale.sale.dueEpochDay)
            assertEquals(listOf(context.clientId to dueDate), context.scheduler.scheduled)
        }
    }

    @Test
    fun `paid sale stamps paidAt and does not schedule a reminder`() = runTest {
        val context = createContext()

        context.viewModel.clientWithSales.test {
            awaitItem()
            assertTrue(
                context.viewModel.registerSale(
                    itemValido(),
                    PaymentMethod.PIX,
                    isPaid = true,
                    dueEpochDay = null
                )
            )

            val sale = awaitItem()!!.sales.single().sale
            assertEquals(PaymentStatus.PAID, sale.status)
            assertNotNull(sale.paidAt)
            assertTrue(context.scheduler.scheduled.isEmpty())
        }
    }

    @Test
    fun `marking the only pending sale paid cancels its grouped reminder`() = runTest {
        val context = createContext()
        val dueDate = LocalDate.now().plusDays(1).toEpochDay()

        context.viewModel.clientWithSales.test {
            awaitItem()
            context.viewModel.registerSale(
                itemValido(),
                PaymentMethod.PIX,
                isPaid = false,
                dueEpochDay = dueDate
            )
            val sale = awaitItem()!!.sales.single().sale

            context.viewModel.markAsPaid(sale)
            val updated = awaitItem()!!.sales.single().sale
            assertEquals(PaymentStatus.PAID, updated.status)
            assertNotNull(updated.paidAt)
            assertEquals(dueDate, updated.dueEpochDay)
            assertEquals(listOf(context.clientId to dueDate), context.scheduler.cancelled)
        }
    }

    @Test
    fun `charge preselects overdue and today sales and totals only selected sales`() = runTest {
        val context = createContext()
        val today = LocalDate.now().toEpochDay()
        context.repository.saveTestSale(
            context.clientId,
            "Atrasada",
            1_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today - 1)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Hoje",
            2_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Futura",
            3_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today + 1)
        )

        context.viewModel.charge.test {
            awaitItem()
            context.viewModel.startCharge()
            val inicial = awaitItem().let { state ->
                if (state.isOpen) state else awaitItem()
            }
            assertEquals(2, inicial.sales.count(ChargeSaleUiModel::isSelected))
            assertEquals(3_000L, inicial.selectedTotalCents)

            context.viewModel.selectAllCharges(true)
            val todas = awaitItem()
            assertTrue(todas.allSelected)
            assertEquals(6_000L, todas.selectedTotalCents)

            val futura = todas.sales.single {
                it.sale.sale.dueEpochDay == today + 1
            }
            context.viewModel.setSaleForCharge(
                futura.sale.sale.id,
                isSelected = false
            )
            val ajustada = awaitItem()
            assertEquals(3_000L, ajustada.selectedTotalCents)

            val atrasada = ajustada.sales.single {
                it.sale.sale.dueEpochDay == today - 1
            }
            context.viewModel.setSaleForCharge(
                atrasada.sale.sale.id,
                isSelected = false
            )
            assertEquals(2_000L, awaitItem().selectedTotalCents)
        }
    }

    @Test
    fun `confirm charge emits message with selected sales only`() = runTest {
        val context = createContext()
        val today = LocalDate.now().toEpochDay()
        context.repository.saveTestSale(
            context.clientId,
            "Selecionada",
            1_500,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Não selecionada",
            4_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today + 1)
        )

        context.viewModel.charge.test {
            awaitItem()
            context.viewModel.startCharge()
            val isOpen = awaitItem().let { if (it.isOpen) it else awaitItem() }
            assertEquals(1_500L, isOpen.selectedTotalCents)

            val dispatch = async { context.viewModel.chargeDispatches.first() }
            context.viewModel.confirmCharge()
            val message = dispatch.await()
            assertTrue(message.message.contains("Selecionada"))
            assertFalse(message.message.contains("Não selecionada"))
            assertTrue(message.message.contains("R$"))
        }
    }

    @Test
    fun `charge leaves every future sale unselected`() = runTest {
        val context = createContext()
        val today = LocalDate.now().toEpochDay()
        listOf(1L, 4L, 21L).forEachIndexed { index, days ->
            context.repository.saveTestSale(
                context.clientId,
                "Futura ${index + 1}",
                (index + 1) * 1_000L,
                PaymentStatus.PENDING,
                SaleTestData(dueEpochDay = today + days)
            )
        }

        context.viewModel.charge.test {
            awaitItem()
            context.viewModel.startCharge()
            val state = awaitItem().let { if (it.isOpen) it else awaitItem() }
            assertTrue(state.sales.none(ChargeSaleUiModel::isSelected))
            assertEquals(0L, state.selectedTotalCents)
            assertFalse(state.allSelected)
        }
    }

    @Test
    fun `history sorts newest first and filters reactively by status`() = runTest {
        val context = createContext()
        context.repository.saveTestSale(
            context.clientId,
            "Pendente antiga",
            1_000,
            PaymentStatus.PENDING,
            SaleTestData(createdAt = 1_000)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Paga recente",
            2_000,
            PaymentStatus.PAID,
            SaleTestData(createdAt = 2_000, paidAt = 3_000)
        )

        context.viewModel.history.test {
            var allSales = awaitItem()
            while (allSales.sales.size < 2) allSales = awaitItem()
            assertEquals(
                listOf("Paga recente", "Pendente antiga"),
                allSales.sales.map { it.items.single().description }
            )

            context.viewModel.selectHistoryFilter(HistoryFilter.PENDING)
            val pendingSales = awaitItem()
            assertEquals(
                listOf("Pendente antiga"),
                pendingSales.sales.map { it.items.single().description }
            )

            context.viewModel.markAsPaid(pendingSales.sales.single().sale)
            val vazio = awaitItem()
            assertTrue(vazio.sales.isEmpty())

            context.viewModel.selectHistoryFilter(HistoryFilter.PAID)
            val paidSales = awaitItem()
            assertEquals(2, paidSales.sales.size)
            assertTrue(paidSales.sales.all { it.sale.status == PaymentStatus.PAID })
        }
    }

    @Test
    fun `marking sale pending requires a non past date and clears paidAt`() = runTest {
        val context = createContext()
        context.repository.saveTestSale(
            context.clientId,
            "Paga",
            2_000,
            PaymentStatus.PAID,
            SaleTestData(paidAt = System.currentTimeMillis())
        )

        context.viewModel.clientWithSales.test {
            val primeiro = awaitItem()
            val sale = (primeiro ?: awaitItem())!!.sales.single().sale
            assertFalse(
                context.viewModel.markAsPending(
                    sale,
                    LocalDate.now().minusDays(1).toEpochDay()
                )
            )
            val dueDate = LocalDate.now().plusDays(1).toEpochDay()
            assertTrue(context.viewModel.markAsPending(sale, dueDate))

            val updated = awaitItem()!!.sales.single().sale
            assertEquals(PaymentStatus.PENDING, updated.status)
            assertNull(updated.paidAt)
            assertEquals(dueDate, updated.dueEpochDay)
        }
    }

    private fun itemValido() =
        listOf(SaleItemForm("Corte", "1", "40,00"))

    private data class TestContext(
        val viewModel: ClientDetailViewModel,
        val repository: LedgerRepository,
        val scheduler: FakeChargeReminderScheduler,
        val clientId: Long
    )
}
