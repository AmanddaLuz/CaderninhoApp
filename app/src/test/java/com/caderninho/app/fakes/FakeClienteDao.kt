package com.caderninho.app.fakes

import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.local.dao.ClienteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Fake in-memory [ClienteDao] used by ViewModel unit tests. */
class FakeClienteDao(private val vendaDao: FakeVendaDao) : ClienteDao {

    private val clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    private var proximoId = 1L

    override fun observarClientes(): Flow<List<ClienteEntity>> = clientes

    // Combines with the venda flow so a status/value change on a sale re-emits
    // the relation too, matching Room's @Relation observing both tables.
    override fun observarClienteComVendas(clienteId: Long): Flow<ClienteComVendas?> =
        combine(clientes, vendaDao.observarTodasVendas()) { lista, _ ->
            lista.firstOrNull { it.id == clienteId }?.let { cliente ->
                ClienteComVendas(cliente, vendaDao.vendasDoCliente(clienteId))
            }
        }

    override fun observarClientesComVendas(): Flow<List<ClienteComVendas>> =
        combine(clientes, vendaDao.observarTodasVendas()) { lista, _ ->
            lista.map { cliente -> ClienteComVendas(cliente, vendaDao.vendasDoCliente(cliente.id)) }
        }

    override suspend fun obterCliente(clienteId: Long): ClienteEntity? =
        clientes.value.firstOrNull { it.id == clienteId }

    override suspend fun inserir(cliente: ClienteEntity): Long {
        val comId = if (cliente.id == 0L) cliente.copy(id = proximoId++) else cliente
        clientes.value = clientes.value.filterNot { it.id == comId.id } + comId
        return comId.id
    }

    override suspend fun atualizar(cliente: ClienteEntity) {
        clientes.value = clientes.value.map { if (it.id == cliente.id) cliente else it }
    }

    override suspend fun remover(cliente: ClienteEntity) {
        clientes.value = clientes.value.filterNot { it.id == cliente.id }
    }
}
