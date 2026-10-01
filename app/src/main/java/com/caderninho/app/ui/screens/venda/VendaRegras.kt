package com.caderninho.app.ui.screens.venda

import com.caderninho.app.data.local.VendaComItens
import com.caderninho.app.data.local.VendaItemEntity
import com.caderninho.app.util.Formatadores
import com.caderninho.app.util.ValoresNumericos

data class ValidacaoItensVenda(
    val itens: List<VendaItemEntity> = emptyList(),
    val erro: String? = null
)

object VendaRegras {

    fun validarItens(formulario: List<ItemVendaFormulario>): ValidacaoItensVenda {
        val itens = formulario.mapIndexedNotNull { indice, item ->
            val quantidade = ValoresNumericos.quantidade(item.quantidade)
            val valorCentavos = ValoresNumericos.centavos(item.valorUnitario)
            if (item.descricao.isBlank() || quantidade == null || valorCentavos == null) {
                null
            } else {
                VendaItemEntity(
                    vendaId = 0,
                    descricao = item.descricao.trim(),
                    quantidade = quantidade,
                    valorUnitarioCentavos = valorCentavos,
                    ordem = indice
                )
            }
        }
        return when {
            formulario.isEmpty() -> ValidacaoItensVenda(erro = "Adicione pelo menos um item.")
            itens.size != formulario.size -> ValidacaoItensVenda(
                erro = "Preencha descrição, quantidade e valor de todos os itens."
            )
            else -> ValidacaoItensVenda(itens = itens)
        }
    }

    fun criarMensagemCobranca(nome: String, vendas: List<VendaComItens>): String {
        val linhas = vendas.joinToString(separator = "\n") { venda ->
            val itens = venda.itens.joinToString { it.descricao }
            "- $itens: ${Formatadores.moedaCentavos(venda.totalCentavos)}"
        }
        val total = vendas.sumOf { it.totalCentavos }
        return "Olá $nome! Passando para lembrar destas compras em aberto:\n" +
            "$linhas\nTotal: ${Formatadores.moedaCentavos(total)}. Obrigado!"
    }
}
