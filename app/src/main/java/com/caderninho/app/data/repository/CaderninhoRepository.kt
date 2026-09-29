package com.caderninho.app.data.repository

import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.dao.ClienteDao
import com.caderninho.app.data.local.dao.VendaDao
import com.caderninho.app.domain.model.StatusPagamento
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única de verdade para clientes e vendas.
 * Hoje persiste apenas localmente (Room); a sincronização com o Firebase
 * será adicionada aqui numa fase futura, sem impactar quem consome o repositório.
 */
@Singleton
class CaderninhoRepository @Inject constructor(
    private val clienteDao: ClienteDao,
    private val vendaDao: VendaDao
) {
    fun observarClientes(): Flow<List<ClienteEntity>> = clienteDao.observarClientes()

    fun observarClientesComVendas(): Flow<List<ClienteComVendas>> =
        clienteDao.observarClientesComVendas()

    fun observarClienteComVendas(clienteId: Long): Flow<ClienteComVendas?> =
        clienteDao.observarClienteComVendas(clienteId)

    fun observarVendasPorStatus(status: StatusPagamento): Flow<List<VendaEntity>> =
        vendaDao.observarVendasPorStatus(status)

    fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaEntity>> =
        vendaDao.observarVendasNoPeriodo(inicio, fim)

    suspend fun salvarCliente(cliente: ClienteEntity): Long = clienteDao.inserir(cliente)

    suspend fun removerCliente(cliente: ClienteEntity) = clienteDao.remover(cliente)

    suspend fun salvarVenda(venda: VendaEntity): Long = vendaDao.inserir(venda)

    suspend fun removerVenda(venda: VendaEntity) = vendaDao.remover(venda)

    suspend fun marcarStatus(vendaId: Long, status: StatusPagamento, pagoEm: Long?) =
        vendaDao.marcarStatus(vendaId, status, pagoEm)
}
