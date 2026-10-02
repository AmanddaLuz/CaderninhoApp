package com.caderninho.app.ui.screens.summary

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.fakes.FakeClientDao
import com.caderninho.app.fakes.FakeSaleDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.SaleTestData
import com.caderninho.app.fakes.saveTestSale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

@ExperimentalCoroutinesApi
class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneId.systemDefault()

    @Test
    fun `monthly summary uses paidAt for received and createdAt for pending`() = runTest {
        val context = createContext()
        val currentMonth = YearMonth.now()
        val inCurrentMonth = currentMonth.atDay(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val inPreviousMonth = currentMonth.minusMonths(1)
            .atDay(10).atStartOfDay(zone).toInstant().toEpochMilli()
        val inNextMonth = currentMonth.plusMonths(1)
            .atDay(10).atStartOfDay(zone).toInstant().toEpochMilli()

        context.repository.saveTestSale(
            context.clientId,
            "Recebida neste mês",
            10_000,
            PaymentStatus.PAID,
            SaleTestData(
                paymentMethod = PaymentMethod.PIX,
                createdAt = inPreviousMonth,
                paidAt = inCurrentMonth
            )
        )
        context.repository.saveTestSale(
            context.clientId,
            "Pendente deste mês",
            5_000,
            PaymentStatus.PENDING,
            SaleTestData(createdAt = inCurrentMonth)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Criada neste mês e paga depois",
            99_900,
            PaymentStatus.PAID,
            SaleTestData(createdAt = inCurrentMonth, paidAt = inNextMonth)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Pendente anterior",
            88_800,
            PaymentStatus.PENDING,
            SaleTestData(createdAt = inPreviousMonth)
        )

        val viewModel = SummaryViewModel(context.repository)

        viewModel.state.test {
            val state = awaitMatching { it.saleCount == 2 }
            assertEquals(10_000L, state.totalReceivedCents)
            assertEquals(5_000L, state.totalPendingCents)
            assertEquals(10_000L, state.byPaymentMethodCents[PaymentMethod.PIX])
        }
    }

    @Test
    fun `daily summary respects start inclusive and end exclusive`() = runTest {
        val context = createContext()
        val today = LocalDate.now()
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val tomorrow = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        context.repository.saveTestSale(
            context.clientId,
            "Recebida no início do dia",
            3_000,
            PaymentStatus.PAID,
            SaleTestData(createdAt = start - 1, paidAt = start)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Pendente no fim do dia",
            2_000,
            PaymentStatus.PENDING,
            SaleTestData(createdAt = tomorrow - 1)
        )
        context.repository.saveTestSale(
            context.clientId,
            "Pendente amanhã",
            7_000,
            PaymentStatus.PENDING,
            SaleTestData(createdAt = tomorrow)
        )

        val viewModel = SummaryViewModel(context.repository)
        viewModel.selectPeriod(SummaryPeriod.DAY)

        viewModel.state.test {
            val state = awaitMatching {
                it.period == SummaryPeriod.DAY && it.saleCount == 2
            }
            assertEquals(3_000L, state.totalReceivedCents)
            assertEquals(2_000L, state.totalPendingCents)
        }
    }

    @Test
    fun `period navigation follows selected day or month`() = runTest {
        val context = createContext()
        val viewModel = SummaryViewModel(context.repository)

        viewModel.state.test {
            val initial = awaitMatching { it.period == SummaryPeriod.MONTH }
            viewModel.previousPeriod()
            val previousMonth = awaitMatching {
                it.selectedDate == initial.selectedDate.minusMonths(1)
            }
            assertEquals(SummaryPeriod.MONTH, previousMonth.period)

            viewModel.selectPeriod(SummaryPeriod.DAY)
            val selectedDay = awaitMatching { it.period == SummaryPeriod.DAY }
            viewModel.nextPeriod()
            awaitMatching { it.selectedDate == selectedDay.selectedDate.plusDays(1) }
        }
    }

    private suspend fun ReceiveTurbine<SummaryUiState>.awaitMatching(
        predicate: (SummaryUiState) -> Boolean
    ): SummaryUiState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }

    private suspend fun createContext(): TestContext {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        val repository = LedgerRepository(clientDao, saleDao)
        val clientId = repository.saveClient(
            ClientEntity(name = "Carlos", phone = "11966665555")
        )
        return TestContext(repository, clientId)
    }

    private data class TestContext(
        val repository: LedgerRepository,
        val clientId: Long
    )
}
