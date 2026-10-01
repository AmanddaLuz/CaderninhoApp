package com.caderninho.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** Cliente junto com suas vendas/serviços, usado para telas de detalhe e resumo. */
data class ClientWithSales(
    @Embedded val client: ClientEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "clienteId",
        entity = SaleEntity::class
    )
    val sales: List<SaleWithItems>
)
