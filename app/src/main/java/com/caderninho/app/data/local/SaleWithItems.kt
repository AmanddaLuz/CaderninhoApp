package com.caderninho.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SaleWithItems(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vendaId"
    )
    val items: List<SaleItemEntity>
) {
    val totalCents: Long
        get() = items.sumOf(SaleItemEntity::subtotalCents)
}
