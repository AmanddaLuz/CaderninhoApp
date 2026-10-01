package com.caderninho.app.fakes

import com.caderninho.app.notification.LembreteCobrancaScheduler

class FakeLembreteCobrancaScheduler : LembreteCobrancaScheduler {
    val agendados = mutableListOf<Pair<Long, Long>>()
    val cancelados = mutableListOf<Pair<Long, Long>>()

    override fun agendar(clienteId: Long, vencimentoEpochDay: Long) {
        agendados += clienteId to vencimentoEpochDay
    }

    override fun cancelar(clienteId: Long, vencimentoEpochDay: Long) {
        cancelados += clienteId to vencimentoEpochDay
    }
}
