package com.caderninho.app.data.local

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
            entity = VendaEntity::class,
            parentColumns = ["id"],
            childColumns = ["vendaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vendaId")]
)
data class VendaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vendaId: Long,
    val descricao: String,
    val quantidade: Double,
    val valorUnitarioCentavos: Long,
    val ordem: Int
) {
    val subtotalCentavos: Long
        get() = BigDecimal.valueOf(quantidade)
            .multiply(BigDecimal.valueOf(valorUnitarioCentavos))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
}
