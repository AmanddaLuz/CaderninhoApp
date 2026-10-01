package com.caderninho.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus

/** Representa uma venda ou serviço prestado a um cliente, pago ou pendente (fiado). */
@Entity(
    tableName = "vendas",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("clienteId"),
        Index(value = ["clienteId", "status", "vencimentoEpochDay"])
    ]
)
@TypeConverters(Converters::class)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "clienteId")
    val clientId: Long,
    @ColumnInfo(name = "formaPagamento")
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    @ColumnInfo(name = "vencimentoEpochDay")
    val dueEpochDay: Long? = null,
    @ColumnInfo(name = "criadoEm")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "pagoEm")
    val paidAt: Long? = null
)
