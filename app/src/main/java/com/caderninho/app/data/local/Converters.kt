package com.caderninho.app.data.local

import androidx.room.TypeConverter
import com.caderninho.app.domain.model.PaymentMethod
import com.caderninho.app.domain.model.PaymentStatus

/** Conversores para o Room persistir os enums do domínio. */
class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = when (value) {
        PaymentMethod.CASH -> "DINHEIRO"
        PaymentMethod.PIX -> "PIX"
        PaymentMethod.CREDIT_CARD -> "CARTAO_CREDITO"
        PaymentMethod.DEBIT_CARD -> "CARTAO_DEBITO"
        PaymentMethod.OTHER -> "OUTRO"
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = when (value) {
        "DINHEIRO", "CASH" -> PaymentMethod.CASH
        "PIX" -> PaymentMethod.PIX
        "CARTAO_CREDITO", "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
        "CARTAO_DEBITO", "DEBIT_CARD" -> PaymentMethod.DEBIT_CARD
        "OUTRO", "OTHER" -> PaymentMethod.OTHER
        else -> error("Unsupported payment method: $value")
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = when (value) {
        PaymentStatus.PAID -> "PAGO"
        PaymentStatus.PENDING -> "PENDENTE"
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = when (value) {
        "PAGO", "PAID" -> PaymentStatus.PAID
        "PENDENTE", "PENDING" -> PaymentStatus.PENDING
        else -> error("Unsupported payment status: $value")
    }
}
