package com.caderninho.app.ui.screens.clients

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
class ClientListEditControllerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRepository(): LedgerRepository {
        val saleDao = FakeSaleDao()
        val clientDao = FakeClientDao(saleDao)
        return LedgerRepository(clientDao, saleDao)
    }

    @Test
    fun `update rejects a blank name`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val client = ClientEntity(id = clientId, name = "Joao", phone = "11988887777")
        val controller = ClientListEditController(repository, { listOf(client) }, this)

        val result = controller.update(client, name = "   ", phone = "11988887777", cpf = "")

        assertTrue(!result)
        assertEquals("Informe o nome do cliente.", controller.formError.value)
    }

    @Test
    fun `update rejects an invalid cpf`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val client = ClientEntity(id = clientId, name = "Joao", phone = "11988887777")
        val controller = ClientListEditController(repository, { listOf(client) }, this)

        val result = controller.update(client, name = "Joao", phone = "11988887777", cpf = "11111111111")

        assertTrue(!result)
        assertEquals(
            "Informe um CPF válido ou deixe o campo vazio.",
            controller.formError.value
        )
    }

    @Test
    fun `update rejects a cpf already used by another client`() = runTest {
        val repository = createRepository()
        val otherId = repository.saveClient(
            ClientEntity(name = "Maria", phone = "11977776666", cpf = "52998224725")
        )
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val other = ClientEntity(id = otherId, name = "Maria", phone = "11977776666", cpf = "52998224725")
        val client = ClientEntity(id = clientId, name = "Joao", phone = "11988887777")
        val controller = ClientListEditController(repository, { listOf(other, client) }, this)

        val result = controller.update(
            client,
            name = "Joao",
            phone = "11988887777",
            cpf = "52998224725"
        )

        assertTrue(!result)
        assertEquals(
            "Este CPF já está cadastrado para outro cliente.",
            controller.formError.value
        )
    }

    @Test
    fun `update allows keeping the same cpf for the same client`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(
            ClientEntity(name = "Joao", phone = "11988887777", cpf = "52998224725")
        )
        val client = ClientEntity(
            id = clientId,
            name = "Joao",
            phone = "11988887777",
            cpf = "52998224725"
        )
        val controller = ClientListEditController(repository, { listOf(client) }, this)

        val result = controller.update(
            client,
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
    fun `clearError resets the current form error`() = runTest {
        val repository = createRepository()
        val clientId = repository.saveClient(ClientEntity(name = "Joao", phone = "11988887777"))
        val client = ClientEntity(id = clientId, name = "Joao", phone = "11988887777")
        val controller = ClientListEditController(repository, { listOf(client) }, this)

        controller.update(client, name = "", phone = "11988887777", cpf = "")
        assertEquals("Informe o nome do cliente.", controller.formError.value)

        controller.clearError()

        assertNull(controller.formError.value)
    }
}
