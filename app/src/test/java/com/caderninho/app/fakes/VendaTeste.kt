package com.caderninho.app.fakes

import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.VendaItemEntity
import com.caderninho.app.data.repository.CaderninhoRepository
import com.caderninho.app.domain.model.FormaPagamento
import com.caderninho.app.domain.model.StatusPagamento

data class VendaTeste(
    val formaPagamento: FormaPagamento = FormaPagamento.DINHEIRO,
    val vencimentoEpochDay: Long? = null,
    val criadoEm: Long = System.currentTimeMillis(),
    val pagoEm: Long? = null
)

suspend fun CaderninhoRepository.salvarVendaTeste(
    clienteId: Long,
    descricao: String,
    valorCentavos: Long,
    status: StatusPagamento,
    dados: VendaTeste = VendaTeste()
): Long = salvarVenda(
    venda = VendaEntity(
        clienteId = clienteId,
        formaPagamento = dados.formaPagamento,
        status = status,
        vencimentoEpochDay = dados.vencimentoEpochDay,
        criadoEm = dados.criadoEm,
        pagoEm = dados.pagoEm
    ),
    itens = listOf(
        VendaItemEntity(
            vendaId = 0,
            descricao = descricao,
            quantidade = 1.0,
            valorUnitarioCentavos = valorCentavos,
            ordem = 0
        )
    )
)
