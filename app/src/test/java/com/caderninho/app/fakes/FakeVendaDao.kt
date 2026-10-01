package com.caderninho.app.fakes

import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.VendaItemEntity
import com.caderninho.app.data.local.dao.VendaDao
import com.caderninho.app.domain.model.StatusPagamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVendaDao : VendaDao {

    private val vendas = MutableStateFlow<List<VendaComItens>>(emptyList())
    private var proximoId = 1L
    private var proximoItemId = 1L

    fun vendasDoCliente(clienteId: Long): List<VendaComItens> =
        vendas.value.filter { it.venda.clienteId == clienteId }
            .sortedByDescending { it.venda.criadoEm }

    override fun observarTodasVendas(): Flow<List<VendaComItens>> =
        vendas.map { lista -> lista.sortedByDescending { it.venda.criadoEm } }

    override fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaComItens>> =
        vendas.map { lista ->
            lista.filter { it.venda.criadoEm in inicio..fim }
                .sortedByDescending { it.venda.criadoEm }
        }

    override suspend fun obterPendentesNaData(
        clienteId: Long,
        vencimentoEpochDay: Long
    ): List<VendaComItens> = vendas.value.filter {
        it.venda.clienteId == clienteId &&
            it.venda.status == StatusPagamento.PENDENTE &&
            it.venda.vencimentoEpochDay == vencimentoEpochDay
    }

    override suspend fun inserir(venda: VendaEntity): Long {
        val comId = if (venda.id == 0L) venda.copy(id = proximoId++) else venda
        vendas.value = vendas.value.filterNot { it.venda.id == comId.id } +
            VendaComItens(comId, emptyList())
        return comId.id
    }

    override suspend fun inserirItens(itens: List<VendaItemEntity>) {
        val porVenda = itens.groupBy(VendaItemEntity::vendaId)
        vendas.value = vendas.value.map { venda ->
            val novosItens = porVenda[venda.venda.id].orEmpty().map { item ->
                if (item.id == 0L) item.copy(id = proximoItemId++) else item
            }
            if (novosItens.isEmpty()) venda else venda.copy(itens = venda.itens + novosItens)
        }
    }

    override suspend fun inserirComItens(
        venda: VendaEntity,
        itens: List<VendaItemEntity>
    ): Long {
        val comId = if (venda.id == 0L) venda.copy(id = proximoId++) else venda
        val itensComId = itens.map { item ->
            val comItemId = if (item.id == 0L) item.copy(id = proximoItemId++) else item
            comItemId.copy(vendaId = comId.id)
        }
        vendas.value = vendas.value.filterNot { it.venda.id == comId.id } +
            VendaComItens(comId, itensComId)
        return comId.id
    }

    override suspend fun remover(venda: VendaEntity) {
        vendas.value = vendas.value.filterNot { it.venda.id == venda.id }
    }

    override suspend fun marcarStatus(
        vendaId: Long,
        status: StatusPagamento,
        pagoEm: Long?,
        vencimentoEpochDay: Long?
    ) {
        vendas.value = vendas.value.map {
            if (it.venda.id == vendaId) {
                it.copy(
                    venda = it.venda.copy(
                        status = status,
                        pagoEm = pagoEm,
                        vencimentoEpochDay = vencimentoEpochDay
                    )
                )
            } else {
                it
            }
        }
    }
}
