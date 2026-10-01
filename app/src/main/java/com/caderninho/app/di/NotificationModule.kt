package com.caderninho.app.di

import com.caderninho.app.notification.ChargeReminderScheduler
import com.caderninho.app.notification.WorkManagerChargeReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    fun provideChargeReminderScheduler(
        implementation: WorkManagerChargeReminderScheduler
    ): ChargeReminderScheduler = implementation
}
