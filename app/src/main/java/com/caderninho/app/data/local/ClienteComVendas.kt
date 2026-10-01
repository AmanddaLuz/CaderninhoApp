package com.caderninho.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** Cliente junto com suas vendas/serviços, usado para telas de detalhe e resumo. */
data class ClienteComVendas(
    @Embedded val cliente: ClienteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "clienteId",
        entity = VendaEntity::class
    )
    val vendas: List<VendaComItens>
)
