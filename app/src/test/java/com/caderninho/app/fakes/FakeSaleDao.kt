package com.caderninho.app.fakes

import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.data.local.dao.SaleDao
import com.caderninho.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSaleDao : SaleDao {

    private val sales = MutableStateFlow<List<SaleWithItems>>(emptyList())
    private var nextId = 1L
    private var nextItemId = 1L

    fun salesForClient(clientId: Long): List<SaleWithItems> =
        sales.value.filter { it.sale.clientId == clientId }
            .sortedByDescending { it.sale.createdAt }

    override fun observeAllSales(): Flow<List<SaleWithItems>> =
        sales.map { list -> list.sortedByDescending { it.sale.createdAt } }

    override fun observeSalesForSummary(
        start: Long,
        endExclusive: Long
    ): Flow<List<SaleWithItems>> =
        sales.map { list ->
            list.filter {
                when (it.sale.status) {
                    PaymentStatus.PAID ->
                        it.sale.paidAt != null && it.sale.paidAt in start until endExclusive
                    PaymentStatus.PENDING ->
                        it.sale.createdAt in start until endExclusive
                }
            }
                .sortedByDescending { it.sale.createdAt }
        }

    override suspend fun getPendingOnDate(
        clientId: Long,
        dueEpochDay: Long
    ): List<SaleWithItems> = sales.value.filter {
        it.sale.clientId == clientId &&
            it.sale.status == PaymentStatus.PENDING &&
            it.sale.dueEpochDay == dueEpochDay
    }

    override suspend fun insert(sale: SaleEntity): Long {
        val withId = if (sale.id == 0L) sale.copy(id = nextId++) else sale
        sales.value = sales.value.filterNot { it.sale.id == withId.id } +
            SaleWithItems(withId, emptyList())
        return withId.id
    }

    override suspend fun insertItems(items: List<SaleItemEntity>) {
        val bySale = items.groupBy(SaleItemEntity::saleId)
        sales.value = sales.value.map { sale ->
            val newItems = bySale[sale.sale.id].orEmpty().map { item ->
                if (item.id == 0L) item.copy(id = nextItemId++) else item
            }
            if (newItems.isEmpty()) sale else sale.copy(items = sale.items + newItems)
        }
    }

    override suspend fun insertWithItems(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): Long {
        val withId = if (sale.id == 0L) sale.copy(id = nextId++) else sale
        val itemsWithIds = items.map { item ->
            val withItemId = if (item.id == 0L) item.copy(id = nextItemId++) else item
            withItemId.copy(saleId = withId.id)
        }
        sales.value = sales.value.filterNot { it.sale.id == withId.id } +
            SaleWithItems(withId, itemsWithIds)
        return withId.id
    }

    override suspend fun delete(sale: SaleEntity) {
        sales.value = sales.value.filterNot { it.sale.id == sale.id }
    }

    override suspend fun updateStatus(
        saleId: Long,
        status: PaymentStatus,
        paidAt: Long?,
        dueEpochDay: Long?
    ) {
        sales.value = sales.value.map {
            if (it.sale.id == saleId) {
                it.copy(
                    sale = it.sale.copy(
                        status = status,
                        paidAt = paidAt,
                        dueEpochDay = dueEpochDay
                    )
                )
            } else {
                it
            }
        }
    }
}
