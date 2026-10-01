package com.caderninho.app.notification

import com.caderninho.app.data.repository.CaderninhoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LembreteCobrancaCoordinator @Inject constructor(
    private val repository: CaderninhoRepository,
    private val scheduler: LembreteCobrancaScheduler
) {
    suspend fun reconciliar(clienteId: Long, vencimentoEpochDay: Long?) {
        if (vencimentoEpochDay == null) return
        val possuiPendencias = repository
            .obterPendentesNaData(clienteId, vencimentoEpochDay)
            .isNotEmpty()
        if (possuiPendencias) {
            scheduler.agendar(clienteId, vencimentoEpochDay)
        } else {
            scheduler.cancelar(clienteId, vencimentoEpochDay)
        }
    }
}
