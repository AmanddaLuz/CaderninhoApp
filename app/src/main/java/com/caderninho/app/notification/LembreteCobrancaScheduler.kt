package com.caderninho.app.notification

interface LembreteCobrancaScheduler {
    fun agendar(clienteId: Long, vencimentoEpochDay: Long)
    fun cancelar(clienteId: Long, vencimentoEpochDay: Long)
}
