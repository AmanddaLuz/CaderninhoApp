package com.caderninho.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerChargeReminderScheduler @Inject constructor(
    @ApplicationContext context: Context
) : ChargeReminderScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun schedule(clientId: Long, dueEpochDay: Long) {
        val now = LocalDateTime.now()
        val execution = LocalDate.ofEpochDay(dueEpochDay).atTime(REMINDER_TIME)
        val delayMillis = Duration.between(now, execution).toMillis().coerceAtLeast(0)
        val data = Data.Builder()
            .putLong(ChargeReminderWorker.CLIENT_ID_KEY, clientId)
            .putLong(ChargeReminderWorker.DUE_DATE_KEY, dueEpochDay)
            .build()
        val request = OneTimeWorkRequestBuilder<ChargeReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(clientTag(clientId))
            .build()

        workManager.enqueueUniqueWork(
            workName(clientId, dueEpochDay),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun cancel(clientId: Long, dueEpochDay: Long) {
        workManager.cancelUniqueWork(workName(clientId, dueEpochDay))
    }

    private fun workName(clientId: Long, dueEpochDay: Long): String =
        "lembrete-cobranca-$clientId-$dueEpochDay"

    private fun clientTag(clientId: Long): String = "lembrete-cobranca-cliente-$clientId"

    private companion object {
        val REMINDER_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
