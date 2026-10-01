package com.caderninho.app.di

import com.caderninho.app.notification.ChargeReminderScheduler
import com.caderninho.app.notification.WorkManagerChargeReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindChargeReminderScheduler(
        implementation: WorkManagerChargeReminderScheduler
    ): ChargeReminderScheduler
}
