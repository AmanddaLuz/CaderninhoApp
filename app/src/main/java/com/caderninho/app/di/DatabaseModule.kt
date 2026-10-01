package com.caderninho.app.di

import android.content.Context
import androidx.room.Room
import com.caderninho.app.data.local.CaderninhoDatabase
import com.caderninho.app.data.local.dao.ClienteDao
import com.caderninho.app.data.local.dao.VendaDao
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
    fun provideDatabase(@ApplicationContext context: Context): CaderninhoDatabase =
        Room.databaseBuilder(
            context,
            CaderninhoDatabase::class.java,
            CaderninhoDatabase.DATABASE_NAME
        )
            .addMigrations(CaderninhoDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideClienteDao(database: CaderninhoDatabase): ClienteDao = database.clienteDao()

    @Provides
    fun provideVendaDao(database: CaderninhoDatabase): VendaDao = database.vendaDao()
}
