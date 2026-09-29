package com.caderninho.app.domain.model

/** Forma de pagamento usada em uma venda ou no quitamento de uma pendência. */
enum class FormaPagamento {
    DINHEIRO,
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO,
    OUTRO
}

/** Status de uma venda/serviço em relação ao pagamento. */
enum class StatusPagamento {
    PAGO,
    PENDENTE
}
