package com.caderninho.app.domain.model

/** Forma de pagamento usada em uma venda ou no quitamento de uma pendência. */
enum class PaymentMethod {
    CASH,
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    OTHER
}

/** Status de uma venda/serviço em relação ao pagamento. */
enum class PaymentStatus {
    PAID,
    PENDING
}
