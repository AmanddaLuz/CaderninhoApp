package com.caderninho.app.data.repository

import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.ClientEntity
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.data.local.dao.ClientDao
import com.caderninho.app.data.local.dao.SaleDao
import com.caderninho.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fonte única de verdade para clientes e vendas.
 * Hoje persiste apenas localmente (Room); a sincronização com o Firebase
 * será adicionada aqui numa fase futura, sem impactar quem consome o repositório.
 */
@Singleton
class LedgerRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val saleDao: SaleDao
) {
    fun observeClientsWithSales(): Flow<List<ClientWithSales>> =
        clientDao.observeClientsWithSales()

    fun observeClientWithSales(clientId: Long): Flow<ClientWithSales?> =
        clientDao.observeClientWithSales(clientId)

    fun observeSalesInPeriod(start: Long, end: Long): Flow<List<SaleWithItems>> =
        saleDao.observeSalesInPeriod(start, end)

    suspend fun saveClient(client: ClientEntity): Long = clientDao.insert(client)

    suspend fun getClient(clientId: Long): ClientEntity? = clientDao.getClient(clientId)

    suspend fun deleteClient(client: ClientEntity) = clientDao.delete(client)

    suspend fun saveSale(sale: SaleEntity, items: List<SaleItemEntity>): Long =
        saleDao.insertWithItems(sale, items)

    suspend fun deleteSale(sale: SaleEntity) = saleDao.delete(sale)

    suspend fun updateStatus(
        saleId: Long,
        status: PaymentStatus,
        paidAt: Long?,
        dueEpochDay: Long?
    ) = saleDao.updateStatus(saleId, status, paidAt, dueEpochDay)

    suspend fun getPendingOnDate(
        clientId: Long,
        dueEpochDay: Long
    ): List<SaleWithItems> = saleDao.getPendingOnDate(clientId, dueEpochDay)
}
