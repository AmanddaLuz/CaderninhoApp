package com.caderninho.app.notification

import com.caderninho.app.data.repository.LedgerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChargeReminderCoordinator @Inject constructor(
    private val repository: LedgerRepository,
    private val scheduler: ChargeReminderScheduler
) {
    suspend fun reconcile(clientId: Long, dueEpochDay: Long?) {
        if (dueEpochDay == null) return
        val hasPendingSales = repository
            .getPendingOnDate(clientId, dueEpochDay)
            .isNotEmpty()
        if (hasPendingSales) {
            scheduler.schedule(clientId, dueEpochDay)
        } else {
            scheduler.cancel(clientId, dueEpochDay)
        }
    }
}
