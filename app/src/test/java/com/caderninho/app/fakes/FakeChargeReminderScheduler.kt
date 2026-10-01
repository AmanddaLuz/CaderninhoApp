package com.caderninho.app.fakes

import com.caderninho.app.notification.ChargeReminderScheduler

class FakeChargeReminderScheduler : ChargeReminderScheduler {
    val scheduled = mutableListOf<Pair<Long, Long>>()
    val cancelled = mutableListOf<Pair<Long, Long>>()

    override fun schedule(clientId: Long, dueEpochDay: Long) {
        scheduled += clientId to dueEpochDay
    }

    override fun cancel(clientId: Long, dueEpochDay: Long) {
        cancelled += clientId to dueEpochDay
    }
}
