package com.caderninho.app.notification

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Avisa a tela de detalhe do cliente aberta no momento que uma cobrança foi
 * tocada na notificação, mesmo que o app já esteja aberto nela (quando a
 * navegação por rota não dispara uma nova composição).
 */
@Singleton
class ChargeRequestBus @Inject constructor() {

    private val _requests = MutableSharedFlow<Long>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val requests: SharedFlow<Long> = _requests.asSharedFlow()

    fun request(clientId: Long) {
        _requests.tryEmit(clientId)
    }
}
