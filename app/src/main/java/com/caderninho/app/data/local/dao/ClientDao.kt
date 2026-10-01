package com.caderninho.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.caderninho.app.data.local.ClientWithSales
import com.caderninho.app.data.local.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {

    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observeClients(): Flow<List<ClientEntity>>

    @Transaction
    @Query("SELECT * FROM clientes WHERE id = :clientId")
    fun observeClientWithSales(clientId: Long): Flow<ClientWithSales?>

    @Transaction
    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observeClientsWithSales(): Flow<List<ClientWithSales>>

    @Query("SELECT * FROM clientes WHERE id = :clientId")
    suspend fun getClient(clientId: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}
