package com.caderninho.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaItemEntity
import com.caderninho.app.domain.model.StatusPagamento
import kotlinx.coroutines.flow.Flow

@Dao
interface VendaDao {

    @Transaction
    @Query("SELECT * FROM vendas ORDER BY criadoEm DESC")
    fun observarTodasVendas(): Flow<List<VendaComItens>>

    @Transaction
    @Query(
        """
        SELECT * FROM vendas
        WHERE criadoEm BETWEEN :inicio AND :fim
        ORDER BY criadoEm DESC
        """
    )
    fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaComItens>>

    @Transaction
    @Query(
        """
        SELECT * FROM vendas
        WHERE clienteId = :clienteId
          AND status = 'PENDENTE'
          AND vencimentoEpochDay = :vencimentoEpochDay
        ORDER BY criadoEm ASC
        """
    )
    suspend fun obterPendentesNaData(
        clienteId: Long,
        vencimentoEpochDay: Long
    ): List<VendaComItens>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(venda: VendaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserirItens(itens: List<VendaItemEntity>)

    @Transaction
    suspend fun inserirComItens(venda: VendaEntity, itens: List<VendaItemEntity>): Long {
        val vendaId = inserir(venda)
        inserirItens(itens.map { it.copy(vendaId = vendaId) })
        return vendaId
    }

    @Delete
    suspend fun remover(venda: VendaEntity)

    @Query(
        """
        UPDATE vendas
        SET status = :status,
            pagoEm = :pagoEm,
            vencimentoEpochDay = :vencimentoEpochDay
        WHERE id = :vendaId
        """
    )
    suspend fun marcarStatus(
        vendaId: Long,
        status: StatusPagamento,
        pagoEm: Long?,
        vencimentoEpochDay: Long?
    )
}
