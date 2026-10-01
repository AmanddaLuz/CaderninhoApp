package com.caderninho.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.caderninho.app.data.local.dao.ClienteDao
import com.caderninho.app.data.local.dao.VendaDao

@Database(
    entities = [ClienteEntity::class, VendaEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CaderninhoDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun vendaDao(): VendaDao

    companion object {
        const val DATABASE_NAME = "caderninho.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clientes ADD COLUMN cpf TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_clientes_cpf ON clientes(cpf)")
            }
        }
    }
}
