package com.caderninho.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Representa um cliente cadastrado pelo lojista. */
@Entity(
    tableName = "clientes",
    indices = [Index(value = ["cpf"], unique = true)]
)
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val telefone: String,
    val cpf: String? = null,
    val observacao: String = "",
    val criadoEm: Long = System.currentTimeMillis()
)
