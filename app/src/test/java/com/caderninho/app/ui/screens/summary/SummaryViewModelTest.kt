package com.caderninho.app.ui.screens.summary

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
import java.time.YearMonth
import java.time.ZoneId

@ExperimentalCoroutinesApi
class SummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun currentMonthInstant(): Long =
        YearMonth.now().atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1

    private fun previousMonthInstant(): Long =
        YearMonth.now().minusMonths(1).atDay(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test
    fun `state aggregates only sales from the current month`() = runTest {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        val repository = LedgerRepository(clientDao, saleDao)
        val clientId = repository.saveClient(ClientEntity(name = "Carlos", phone = "11966665555"))

        repository.saveTestSale(
            clientId = clientId,
            description = "Venda deste mes paga",
            valueCents = 10_000,
            status = PaymentStatus.PAID,
            testData = SaleTestData(
                paymentMethod = PaymentMethod.PIX,
                createdAt = currentMonthInstant(),
                paidAt = currentMonthInstant()
            )
        )
        repository.saveTestSale(
            clientId = clientId,
            description = "Venda deste mes pendente",
            valueCents = 5_000,
            status = PaymentStatus.PENDING,
            testData = SaleTestData(createdAt = currentMonthInstant())
        )
        repository.saveTestSale(
            clientId = clientId,
            description = "Venda do mes passado",
            valueCents = 99_900,
            status = PaymentStatus.PAID,
            testData = SaleTestData(
                paymentMethod = PaymentMethod.PIX,
                createdAt = previousMonthInstant(),
                paidAt = previousMonthInstant()
            )
        )

        val viewModel = SummaryViewModel(repository)

        viewModel.state.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(100.0, state.totalReceived, 0.0)
            assertEquals(50.0, state.totalPending, 0.0)
            assertEquals(2, state.saleCount)
            assertEquals(100.0, state.byPaymentMethod[PaymentMethod.PIX])
        }
    }
}
