package com.caderninho.app.fakes

import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.local.dao.ClientDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Fake in-memory [ClientDao] used by ViewModel unit tests. */
class FakeClientDao(private val saleDao: FakeSaleDao) : ClientDao {

    private val clients = MutableStateFlow<List<ClientEntity>>(emptyList())
    private var nextId = 1L

    override fun observeClients(): Flow<List<ClientEntity>> = clients

    // Combines with the venda flow so a status/value change on a sale re-emits
    // the relation too, matching Room's @Relation observing both tables.
    override fun observeClientWithSales(clientId: Long): Flow<ClientWithSales?> =
        combine(clients, saleDao.observeAllSales()) { list, _ ->
            list.firstOrNull { it.id == clientId }?.let { client ->
                ClientWithSales(client, saleDao.salesForClient(clientId))
            }
        }

    override fun observeClientsWithSales(): Flow<List<ClientWithSales>> =
        combine(clients, saleDao.observeAllSales()) { list, _ ->
            list.map { client -> ClientWithSales(client, saleDao.salesForClient(client.id)) }
        }

    override suspend fun getClient(clientId: Long): ClientEntity? =
        clients.value.firstOrNull { it.id == clientId }

    override suspend fun insert(client: ClientEntity): Long {
        val withId = if (client.id == 0L) client.copy(id = nextId++) else client
        clients.value = clients.value.filterNot { it.id == withId.id } + withId
        return withId.id
    }

    override suspend fun update(client: ClientEntity) {
        clients.value = clients.value.map { if (it.id == client.id) client else it }
    }

    override suspend fun delete(client: ClientEntity) {
        clients.value = clients.value.filterNot { it.id == client.id }
    }
}
