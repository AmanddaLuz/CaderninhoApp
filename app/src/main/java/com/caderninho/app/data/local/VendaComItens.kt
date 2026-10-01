package com.caderninho.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class VendaComItens(
    @Embedded val venda: VendaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vendaId"
    )
    val itens: List<VendaItemEntity>
) {
    val totalCentavos: Long
        get() = itens.sumOf(VendaItemEntity::subtotalCentavos)
}
