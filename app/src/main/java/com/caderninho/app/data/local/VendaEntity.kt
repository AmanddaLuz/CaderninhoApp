package com.caderninho.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento

/** Representa uma venda ou serviço prestado a um cliente, pago ou pendente (fiado). */
@Entity(
    tableName = "vendas",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
@TypeConverters(Converters::class)
data class VendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val descricao: String,
    val valor: Double,
    val formaPagamento: FormaPagamento,
    val status: StatusPagamento,
    val criadoEm: Long = System.currentTimeMillis(),
    val pagoEm: Long? = null
)
