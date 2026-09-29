package com.caderninho.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Representa um cliente cadastrado pelo lojista. */
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val telefone: String,
    val observacao: String = "",
    val criadoEm: Long = System.currentTimeMillis()
)
