package com.caderninho.app.fakes

import com.caderninho.app.data.local.SaleEntity
import com.caderninho.app.data.local.SaleItemEntity
import com.caderninho.app.data.repository.LedgerRepository
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus

data class SaleTestData(
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val dueEpochDay: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)

suspend fun LedgerRepository.saveTestSale(
    clientId: Long,
    description: String,
    valueCents: Long,
    status: PaymentStatus,
    testData: SaleTestData = SaleTestData()
): Long = saveSale(
    sale = SaleEntity(
        clientId = clientId,
        paymentMethod = testData.paymentMethod,
        status = status,
        dueEpochDay = testData.dueEpochDay,
        createdAt = testData.createdAt,
        paidAt = testData.paidAt
    ),
    items = listOf(
        SaleItemEntity(
            saleId = 0,
            description = description,
            quantity = 1.0,
            unitValueCents = valueCents,
            position = 0
        )
    )
)
