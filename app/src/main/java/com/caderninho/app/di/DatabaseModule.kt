package com.caderninho.app.di

import android.content.Context
import androidx.room.Room
import com.caderninho.app.data.local.LedgerDatabase
import com.caderninho.app.data.local.dao.ClientDao
import com.caderninho.app.data.local.dao.SaleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LedgerDatabase =
        Room.databaseBuilder(
            context,
            LedgerDatabase::class.java,
            LedgerDatabase.DATABASE_NAME
        )
            .addMigrations(
                LedgerDatabase.MIGRATION_1_2,
                LedgerDatabase.MIGRATION_2_3
            )
            .build()

    @Provides
    fun provideClientDao(database: LedgerDatabase): ClientDao = database.clientDao()

    @Provides
    fun provideSaleDao(database: LedgerDatabase): SaleDao = database.saleDao()
}
