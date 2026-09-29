package com.caderninho.app.fakes

import com.caderninho.app.data.local.VendaEntity
import com.caderninho.app.data.local.dao.VendaDao
import com.caderninho.app.domain.model.StatusPagamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Fake in-memory [VendaDao] used by ViewModel unit tests. */
class FakeVendaDao : VendaDao {

    private val vendas = MutableStateFlow<List<VendaEntity>>(emptyList())
    private var proximoId = 1L

    fun vendasDoCliente(clienteId: Long): List<VendaEntity> =
        vendas.value.filter { it.clienteId == clienteId }.sortedByDescending { it.criadoEm }

    override fun observarVendasDoCliente(clienteId: Long): Flow<List<VendaEntity>> =
        vendas.map { lista -> lista.filter { it.clienteId == clienteId }.sortedByDescending { it.criadoEm } }

    override fun observarVendasPorStatus(status: StatusPagamento): Flow<List<VendaEntity>> =
        vendas.map { lista -> lista.filter { it.status == status }.sortedByDescending { it.criadoEm } }

    override fun observarTodasVendas(): Flow<List<VendaEntity>> =
        vendas.map { lista -> lista.sortedByDescending { it.criadoEm } }

    override fun observarVendasNoPeriodo(inicio: Long, fim: Long): Flow<List<VendaEntity>> =
        vendas.map { lista ->
            lista.filter { it.criadoEm in inicio..fim }.sortedByDescending { it.criadoEm }
        }

    override suspend fun inserir(venda: VendaEntity): Long {
        val comId = if (venda.id == 0L) venda.copy(id = proximoId++) else venda
        vendas.value = vendas.value.filterNot { it.id == comId.id } + comId
        return comId.id
    }

    override suspend fun atualizar(venda: VendaEntity) {
        vendas.value = vendas.value.map { if (it.id == venda.id) venda else it }
    }

    override suspend fun remover(venda: VendaEntity) {
        vendas.value = vendas.value.filterNot { it.id == venda.id }
    }

    override suspend fun marcarStatus(vendaId: Long, status: StatusPagamento, pagoEm: Long?) {
        vendas.value = vendas.value.map {
            if (it.id == vendaId) it.copy(status = status, pagoEm = pagoEm) else it
        }
    }
}
