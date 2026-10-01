package com.caderninho.app.di

import com.caderninho.app.notification.LembreteCobrancaScheduler
import com.caderninho.app.notification.WorkManagerLembreteCobrancaScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun bindLembreteCobrancaScheduler(
        implementation: WorkManagerLembreteCobrancaScheduler
    ): LembreteCobrancaScheduler
}
