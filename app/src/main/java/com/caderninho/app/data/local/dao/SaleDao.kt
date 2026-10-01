package com.caderninho.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Transaction
    @Query("SELECT * FROM vendas ORDER BY criadoEm DESC")
    fun observeAllSales(): Flow<List<SaleWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM vendas
        WHERE criadoEm BETWEEN :start AND :end
        ORDER BY criadoEm DESC
        """
    )
    fun observeSalesInPeriod(start: Long, end: Long): Flow<List<SaleWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM vendas
        WHERE clienteId = :clientId
          AND status = 'PENDENTE'
          AND vencimentoEpochDay = :dueEpochDay
        ORDER BY criadoEm ASC
        """
    )
    suspend fun getPendingOnDate(
        clientId: Long,
        dueEpochDay: Long
    ): List<SaleWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<SaleItemEntity>)

    @Transaction
    suspend fun insertWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insert(sale)
        insertItems(items.map { it.copy(saleId = saleId) })
        return saleId
    }

    @Delete
    suspend fun delete(sale: SaleEntity)

    @Query(
        """
        UPDATE vendas
        SET status = :status,
            pagoEm = :paidAt,
            vencimentoEpochDay = :dueEpochDay
        WHERE id = :saleId
        """
    )
    suspend fun updateStatus(
        saleId: Long,
        status: PaymentStatus,
        paidAt: Long?,
        dueEpochDay: Long?
    )
}
