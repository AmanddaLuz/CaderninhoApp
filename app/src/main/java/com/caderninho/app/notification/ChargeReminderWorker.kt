package com.caderninho.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caderninho.app.data.repository.LedgerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ChargeReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: LedgerRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val clientId = inputData.getLong(CLIENT_ID_KEY, INVALID_ID)
        val dueDate = inputData.getLong(DUE_DATE_KEY, INVALID_DATE)
        return when {
            clientId == INVALID_ID || dueDate == INVALID_DATE -> Result.failure()
            else -> processReminder(clientId, dueDate)
        }
    }

    private suspend fun processReminder(clientId: Long, dueDate: Long): Result {
        val client = repository.getClient(clientId)
        val pendingSales = repository.getPendingOnDate(clientId, dueDate)
        if (client != null && pendingSales.isNotEmpty()) {
            ChargeNotification.show(
                context = applicationContext,
                clientId = clientId,
                clientName = client.name,
                quantity = pendingSales.size,
                date = dueDate
            )
        }
        return Result.success()
    }

    companion object {
        const val CLIENT_ID_KEY = "cliente_id"
        const val DUE_DATE_KEY = "vencimento_epoch_day"
        private const val INVALID_ID = -1L
        private const val INVALID_DATE = Long.MIN_VALUE
    }
}
