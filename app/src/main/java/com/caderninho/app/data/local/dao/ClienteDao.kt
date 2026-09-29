package com.caderninho.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.caderninho.app.data.local.ClienteComVendas
import com.caderninho.app.data.local.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarClientes(): Flow<List<ClienteEntity>>

    @Transaction
    @Query("SELECT * FROM clientes WHERE id = :clienteId")
    fun observarClienteComVendas(clienteId: Long): Flow<ClienteComVendas?>

    @Transaction
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarClientesComVendas(): Flow<List<ClienteComVendas>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(cliente: ClienteEntity): Long

    @Update
    suspend fun atualizar(cliente: ClienteEntity)

    @Delete
    suspend fun remover(cliente: ClienteEntity)
}
