package com.caderninho.app.ui.screens.sale

import app.cash.turbine.test
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.fakes.FakeClientDao
import com.caderninho.app.fakes.FakeSaleDao
import com.caderninho.app.fakes.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClientEditControllerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRepository(): LedgerRepository {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        return LedgerRepository(clientDao, saleDao)
    }

    @Test
    fun `update rejects a blank name without persisting`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val current = repository.getClient(clientId)
        val controller = ClientEditController(repository, { current }, this)

        val result = controller.update(name = "   ", phone = "11988887777", cpf = "")

        assertTrue(!result)
        assertEquals("Informe o nome do cliente.", controller.formError.value)
    }

    @Test
    fun `update returns false when there is no current client`() = runTest {
        val repository = createRepository()
        val controller = ClientEditController(repository, { null }, this)

        val result = controller.update(name = "Joao", phone = "11988887777", cpf = "")

        assertTrue(!result)
    }

    @Test
    fun `update persists a trimmed name, phone and cpf`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val current = repository.getClient(clientId)
        val controller = ClientEditController(repository, { current }, this)

        val result = controller.update(
            name = " Joao Silva ",
            phone = " 11988887777 ",
            cpf = "529.982.247-25"
        )
        advanceUntilIdle()

        assertTrue(result)
        assertNull(controller.formError.value)
        val saved = repository.getClient(clientId)!!
        assertEquals("Joao Silva", saved.name)
        assertEquals("11988887777", saved.phone)
        assertEquals("52998224725", saved.cpf)
    }

    @Test
    fun `update rejects an invalid cpf`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val current = repository.getClient(clientId)
        val controller = ClientEditController(repository, { current }, this)

        val result = controller.update(name = "Joao", phone = "11988887777", cpf = "11111111111")

        assertTrue(!result)
        assertEquals(
            "Informe um CPF válido ou deixe o campo vazio.",
            controller.formError.value
        )
    }

    @Test
    fun `clearError resets the current form error`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val current = repository.getClient(clientId)
        val controller = ClientEditController(repository, { current }, this)

        controller.update(name = "", phone = "11988887777", cpf = "")
        assertEquals("Informe o nome do cliente.", controller.formError.value)

        controller.clearError()

        assertNull(controller.formError.value)
    }

    @Test
    fun `delete removes the current client and emits deleted`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val current = repository.getClient(clientId)
        val controller = ClientEditController(repository, { current }, this)

        controller.deleted.test {
            controller.delete()
            awaitItem()
            assertNull(repository.getClient(clientId))
        }
    }

    @Test
    fun `delete does nothing when there is no current client`() = runTest {
        val repository = createRepository()
        val controller = ClientEditController(repository, { null }, this)

        controller.delete()
        advanceUntilIdle()
    }
}
