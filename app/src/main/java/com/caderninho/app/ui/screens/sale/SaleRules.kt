package com.caderninho.app.ui.screens.sale

import com.caderninho.app.data.local.SaleWithItems
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.util.Formatters
import com.caderninho.app.util.NumericValues

data class SaleItemsValidation(
    val items: List<SaleItemEntity> = emptyList(),
    val error: String? = null
)

object SaleRules {

    fun validateItems(form: List<SaleItemForm>): SaleItemsValidation {
        val items = form.mapIndexedNotNull { index, item ->
            val quantity = NumericValues.quantity(item.quantity)
            val valueCents = NumericValues.cents(item.unitValue)
            if (item.description.isBlank() || quantity == null || valueCents == null) {
                null
            } else {
                SaleItemEntity(
                    saleId = 0,
                    description = item.description.trim(),
                    quantity = quantity,
                    unitValueCents = valueCents,
                    position = index
                )
            }
        }
        return when {
            form.isEmpty() -> SaleItemsValidation(error = "Adicione pelo menos um item.")
            items.size != form.size -> SaleItemsValidation(
                error = "Preencha descrição, quantidade e valor de todos os itens."
            )
            else -> SaleItemsValidation(items = items)
        }
    }

    fun createChargeMessage(name: String, sales: List<SaleWithItems>): String {
        val lines = sales.joinToString(separator = "\n") { sale ->
            val items = sale.items.joinToString { it.description }
            "- $items: ${Formatters.currencyFromCents(sale.totalCents)}"
        }
        val total = sales.sumOf { it.totalCents }
        return "Olá $name! Passando para lembrar destas compras em aberto:\n" +
            "$lines\nTotal: ${Formatters.currencyFromCents(total)}. Obrigado!"
    }
}
