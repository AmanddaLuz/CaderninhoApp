package com.caderninho.app.data.repository

import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.VendaItemEntity
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
    fun observarClientesComVendas(): Flow<List<ClienteComVendas>> =
        clienteDao.observarClientesComVendas()

    fun observarClienteComVendas(clienteId: Long): Flow<ClienteComVendas?> =
        clienteDao.observarClienteComVendas(clienteId)

    fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaComItens>> =
        vendaDao.observarVendasNoPeriodo(inicio, fim)

    suspend fun salvarCliente(cliente: ClienteEntity): Long = clienteDao.inserir(cliente)

    suspend fun obterCliente(clienteId: Long): ClienteEntity? = clienteDao.obterCliente(clienteId)

    suspend fun removerCliente(cliente: ClienteEntity) = clienteDao.remover(cliente)

    suspend fun salvarVenda(venda: VendaEntity, itens: List<VendaItemEntity>): Long =
        vendaDao.inserirComItens(venda, itens)

    suspend fun removerVenda(venda: VendaEntity) = vendaDao.remover(venda)

    suspend fun marcarStatus(
        vendaId: Long,
        status: StatusPagamento,
        pagoEm: Long?,
        vencimentoEpochDay: Long?
    ) = vendaDao.marcarStatus(vendaId, status, pagoEm, vencimentoEpochDay)

    suspend fun obterPendentesNaData(
        clienteId: Long,
        vencimentoEpochDay: Long
    ): List<VendaComItens> = vendaDao.obterPendentesNaData(clienteId, vencimentoEpochDay)
}
