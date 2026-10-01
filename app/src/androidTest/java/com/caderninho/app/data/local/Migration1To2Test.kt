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
class Migration1To2Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LedgerDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrationPreservesClientsAndAddsOptionalCpf() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                """
                INSERT INTO clientes (id, nome, telefone, observacao, criadoEm)
                VALUES (1, 'Maria', '11999999999', '', 1000)
                """.trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            LedgerDatabase.MIGRATION_1_2
        ).use { database ->
            database.query("SELECT nome, cpf FROM clientes WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Maria", cursor.getString(0))
                assertNull(cursor.getString(1))
            }
        }
    }

    private companion object {
        const val TEST_DATABASE = "migration-1-2-test"
    }
}
