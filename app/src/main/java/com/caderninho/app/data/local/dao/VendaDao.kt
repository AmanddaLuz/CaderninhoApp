package com.caderninho.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.domain.model.StatusPagamento
import kotlinx.coroutines.flow.Flow

@Dao
interface VendaDao {

    @Query("SELECT * FROM vendas WHERE clienteId = :clienteId ORDER BY criadoEm DESC")
    fun observarVendasDoCliente(clienteId: Long): Flow<List<VendaEntity>>

    @Query("SELECT * FROM vendas WHERE status = :status ORDER BY criadoEm DESC")
    fun observarVendasPorStatus(status: StatusPagamento): Flow<List<VendaEntity>>

    @Query("SELECT * FROM vendas ORDER BY criadoEm DESC")
    fun observarTodasVendas(): Flow<List<VendaEntity>>

    @Query(
        """
        SELECT * FROM vendas
        WHERE criadoEm BETWEEN :inicio AND :fim
        ORDER BY criadoEm DESC
        """
    )
    fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(venda: VendaEntity): Long

    @Update
    suspend fun atualizar(venda: VendaEntity)

    @Delete
    suspend fun remover(venda: VendaEntity)

    @Query("UPDATE vendas SET status = :status, pagoEm = :pagoEm WHERE id = :vendaId")
    suspend fun marcarStatus(vendaId: Long, status: StatusPagamento, pagoEm: Long?)
}
