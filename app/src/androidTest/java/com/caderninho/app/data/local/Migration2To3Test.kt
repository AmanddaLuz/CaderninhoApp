package com.caderninho.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2To3Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LedgerDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrationConvertsLegacySaleToSingleItemWithoutLosingValue() {
        helper.createDatabase(TEST_DATABASE, 2).apply {
            execSQL(
                """
                INSERT INTO clientes (id, nome, telefone, cpf, observacao, criadoEm)
                VALUES (1, 'Maria', '11999999999', NULL, '', 1000)
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO vendas (
                    id, clienteId, descricao, valor, formaPagamento, status, criadoEm, pagoEm
                )
                VALUES (10, 1, 'Compra antiga', 12.34, 'DINHEIRO', 'PENDENTE', 2000, NULL)
                """.trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            3,
            true,
            LedgerDatabase.MIGRATION_2_3
        ).use { database ->
            database.query(
                """
                SELECT v.clienteId, v.vencimentoEpochDay, i.descricao,
                       i.quantidade, i.valorUnitarioCentavos
                FROM vendas v
                JOIN venda_itens i ON i.vendaId = v.id
                WHERE v.id = 10
                """.trimIndent()
            ).use { cursor ->
                cursor.moveToFirst()
                assertEquals(1L, cursor.getLong(0))
                assertNull(cursor.getString(1))
                assertEquals("Compra antiga", cursor.getString(2))
                assertEquals(1.0, cursor.getDouble(3), 0.0)
                assertEquals(1_234L, cursor.getLong(4))
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "migration-2-3-test"
    }
}
