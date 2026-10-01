package com.caderninho.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.caderninho.app.data.local.dao.ClienteDao
import com.caderninho.app.data.local.dao.VendaDao

@Database(
    entities = [ClienteEntity::class, VendaEntity::class, VendaItemEntity::class],
    version = 3,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS vendas_nova (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clienteId INTEGER NOT NULL,
                        formaPagamento TEXT NOT NULL,
                        status TEXT NOT NULL,
                        vencimentoEpochDay INTEGER,
                        criadoEm INTEGER NOT NULL,
                        pagoEm INTEGER,
                        FOREIGN KEY(clienteId) REFERENCES clientes(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO vendas_nova (
                        id, clienteId, formaPagamento, status,
                        vencimentoEpochDay, criadoEm, pagoEm
                    )
                    SELECT id, clienteId, formaPagamento, status, NULL, criadoEm, pagoEm
                    FROM vendas
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE venda_itens_temporarios AS
                    SELECT
                        id AS vendaId,
                        descricao,
                        1.0 AS quantidade,
                        CAST(ROUND(valor * 100.0) AS INTEGER) AS valorUnitarioCentavos,
                        0 AS ordem
                    FROM vendas
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE vendas")
                db.execSQL("ALTER TABLE vendas_nova RENAME TO vendas")
                db.execSQL("CREATE INDEX index_vendas_clienteId ON vendas(clienteId)")
                db.execSQL(
                    """
                    CREATE INDEX index_vendas_clienteId_status_vencimentoEpochDay
                    ON vendas(clienteId, status, vencimentoEpochDay)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS venda_itens (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        vendaId INTEGER NOT NULL,
                        descricao TEXT NOT NULL,
                        quantidade REAL NOT NULL,
                        valorUnitarioCentavos INTEGER NOT NULL,
                        ordem INTEGER NOT NULL,
                        FOREIGN KEY(vendaId) REFERENCES vendas(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO venda_itens (
                        vendaId, descricao, quantidade, valorUnitarioCentavos, ordem
                    )
                    SELECT vendaId, descricao, quantidade, valorUnitarioCentavos, ordem
                    FROM venda_itens_temporarios
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX index_venda_itens_vendaId ON venda_itens(vendaId)")
                db.execSQL("DROP TABLE venda_itens_temporarios")
            }
        }
    }
}
