package com.caderninho.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.math.RoundingMode

@Entity(
    tableName = "venda_itens",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vendaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vendaId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "vendaId")
    val saleId: Long,
    @ColumnInfo(name = "descricao")
    val description: String,
    @ColumnInfo(name = "quantidade")
    val quantity: Double,
    @ColumnInfo(name = "valorUnitarioCentavos")
    val unitValueCents: Long,
    @ColumnInfo(name = "ordem")
    val position: Int
) {
    val subtotalCents: Long
        get() = BigDecimal.valueOf(quantity)
            .multiply(BigDecimal.valueOf(unitValueCents))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
}
