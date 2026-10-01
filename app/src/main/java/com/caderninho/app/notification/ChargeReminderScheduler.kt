package com.caderninho.app.notification

interface ChargeReminderScheduler {
    fun schedule(clientId: Long, dueEpochDay: Long)
    fun cancel(clientId: Long, dueEpochDay: Long)
}
