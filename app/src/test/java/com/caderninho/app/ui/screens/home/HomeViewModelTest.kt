package com.caderninho.app.ui.screens.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.fakes.FakeClientDao
import com.caderninho.app.fakes.FakeSaleDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.SaleTestData
import com.caderninho.app.fakes.saveTestSale
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `home aggregates clients and pending sales by due date`() = runTest {
        val repository = createRepository()
        val firstClientId = repository.saveClient(
            ClientEntity(name = "Ana", phone = "11999990000")
        )
        repository.saveClient(ClientEntity(name = "Bia", phone = "11988880000"))
        val today = LocalDate.now().toEpochDay()

        repository.saveTestSale(
            firstClientId,
            "Atrasada",
            1_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today - 1)
        )
        repository.saveTestSale(
            firstClientId,
            "Vence hoje",
            2_000,
            PaymentStatus.PENDING,
            SaleTestData(dueEpochDay = today)
        )
        repository.saveTestSale(
            firstClientId,
            "Sem vencimento",
            3_000,
            PaymentStatus.PENDING
        )
        repository.saveTestSale(
            firstClientId,
            "Paga",
            9_000,
            PaymentStatus.PAID,
            SaleTestData(dueEpochDay = today - 2)
        )

        HomeViewModel(repository).state.test {
            val state = awaitMatching { it.clientCount == 2 && it.pendingSaleCount == 3 }

            assertEquals(1, state.overdueSaleCount)
            assertEquals(1, state.dueTodayCount)
            assertEquals(6_000L, state.pendingTotalCents)
            assertEquals(2_000L, state.dueTodayTotalCents)
            assertFalse(state.valuesVisible)
        }
    }

    @Test
    fun `values start hidden and visibility can be toggled`() = runTest {
        val viewModel = HomeViewModel(createRepository())

        viewModel.state.test {
            assertFalse(awaitItem().valuesVisible)

            viewModel.toggleValuesVisibility()
            assertTrue(awaitMatching(HomeUiState::valuesVisible).valuesVisible)

            viewModel.toggleValuesVisibility()
            assertFalse(awaitMatching { !it.valuesVisible }.valuesVisible)
        }
    }

    @Test
    fun `home state contains no personal client fields`() {
        val propertyNames = HomeUiState::class.java.declaredFields.map { it.name }

        assertFalse(propertyNames.any { it.contains("name", ignoreCase = true) })
        assertFalse(propertyNames.any { it.contains("phone", ignoreCase = true) })
        assertFalse(propertyNames.any { it.contains("cpf", ignoreCase = true) })
    }

    private fun createRepository(): LedgerRepository {
        val saleDao = FakeSaleDao()
        return LedgerRepository(FakeClientDao(saleDao), saleDao)
    }

    private suspend fun ReceiveTurbine<HomeUiState>.awaitMatching(
        predicate: (HomeUiState) -> Boolean
    ): HomeUiState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }
}
