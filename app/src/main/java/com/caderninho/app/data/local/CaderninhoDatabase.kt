package com.caderninho.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.caderninho.app.data.local.dao.ClienteDao
import com.caderninho.app.data.local.dao.VendaDao

@Database(
    entities = [ClienteEntity::class, VendaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CaderninhoDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun vendaDao(): VendaDao

    companion object {
        const val DATABASE_NAME = "caderninho.db"
    }
}
