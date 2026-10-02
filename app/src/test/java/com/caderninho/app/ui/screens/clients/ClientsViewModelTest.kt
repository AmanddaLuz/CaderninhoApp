package com.caderninho.app.ui.screens.clients

import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentStatus
import com.caderninho.app.fakes.FakeClientDao
import com.caderninho.app.fakes.FakeSaleDao
import com.caderninho.app.fakes.MainDispatcherRule
import com.caderninho.app.fakes.SaleTestData
import com.caderninho.app.fakes.saveTestSale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClientsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRepository(): LedgerRepository {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        return LedgerRepository(clientDao, saleDao)
    }

    @Test
    fun `addClient ignores a blank name`() = runTest {
        val repository = createRepository()
        val viewModel = ClientsViewModel(repository)

        val saved = viewModel.addClient(name = "   ", phone = "11999999999", cpf = "")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.clients.isEmpty())
        assertTrue(!saved)
        assertEquals("Informe o nome do cliente.", state.registrationError)
    }

    @Test
    fun `addClient persists a trimmed client`() = runTest {
        val repository = createRepository()
        val viewModel = ClientsViewModel(repository)

        viewModel.addClient(
            name = " Maria ",
            phone = " 11999999999 ",
            cpf = "529.982.247-25"
        )

        advanceUntilIdle()
        val item = viewModel.uiState.value.clients.single()
        assertEquals("Maria", item.client.name)
        assertEquals("11999999999", item.client.phone)
        assertEquals("52998224725", item.client.cpf)
        assertEquals(0L, item.pendingBalanceCents)
    }

    @Test
    fun `pendingBalance sums only pending sales for a client`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        repository.saveTestSale(
            clientId = clientId,
            description = "Corte",
            valueCents = 3_000,
            status = PaymentStatus.PENDING
        )
        repository.saveTestSale(
            clientId = clientId,
            description = "Manicure",
            valueCents = 2_000,
            status = PaymentStatus.PAID,
            testData = SaleTestData(paidAt = System.currentTimeMillis())
        )

        val viewModel = ClientsViewModel(repository)
        advanceUntilIdle()

        val item = viewModel.uiState.value.clients.single()
        assertEquals(3_000L, item.pendingBalanceCents)
    }

    @Test
    fun `deleteClient deletes the client`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val viewModel = ClientsViewModel(repository)
        advanceUntilIdle()

        val client = viewModel.uiState.value.clients.single().client
        viewModel.deleteClient(client)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.clients.isEmpty())
    }

    @Test
    fun `updateQuery filters by normalized name or phone digits`() = runTest {
        val repository = createRepository()
        repository.saveClient(ClientEntity(name = "José Silva", phone = "(11) 98888-7777"))
        repository.saveClient(ClientEntity(name = "Maria", phone = "(21) 97777-6666"))
        val viewModel = ClientsViewModel(repository)
        advanceUntilIdle()

        viewModel.updateQuery("jose")
        advanceUntilIdle()
        assertEquals(listOf("José Silva"), viewModel.uiState.value.clients.map { it.client.name })

        viewModel.updateQuery("2197777")
        advanceUntilIdle()
        assertEquals(listOf("Maria"), viewModel.uiState.value.clients.map { it.client.name })
    }

    @Test
    fun `addClient rejects invalid and duplicate CPF`() = runTest {
        val repository = createRepository()
        val viewModel = ClientsViewModel(repository)

        assertTrue(!viewModel.addClient("Ana", "11999999999", "11111111111"))
        advanceUntilIdle()
        assertEquals(
            "Informe um CPF válido ou deixe o campo vazio.",
            viewModel.uiState.value.registrationError
        )

        assertTrue(viewModel.addClient("Ana", "11999999999", "52998224725"))
        advanceUntilIdle()
        assertTrue(!viewModel.addClient("Outra Ana", "11888888888", "52998224725"))
        advanceUntilIdle()
        assertEquals("Este CPF já está cadastrado.", viewModel.uiState.value.registrationError)
    }
}
